import React, { Component } from 'react'
import PropTypes from 'prop-types'
import {
  Text,
  StyleSheet,
  StatusBar,
  Dimensions,
  BackHandler,
  Animated,
  Image,
  Alert
} from 'react-native'
import Orientation from 'react-native-orientation-locker'
import Icons from 'react-native-vector-icons/MaterialIcons'
import { Controls } from './'
import { checkSource } from './utils'
import { VLCPlayer } from 'react-native-vlc-media-player';
const Win = Dimensions.get('window')
const backgroundColor = '#000'

const styles = StyleSheet.create({
  background: {
    backgroundColor,
    justifyContent: 'center',
    alignItems: 'center',
    zIndex: 98
  },
  fullScreen: {
    //...StyleSheet.absoluteFillObject
    height: '100%',
    width: '100%'
  },
  image: {
    ...StyleSheet.absoluteFillObject,
    width: undefined,
    height: undefined,
    zIndex: 99
  }
})

const defaultTheme = {
  title: '#FFF',
  more: '#FFF',
  center: '#FFF',
  fullscreen: '#FFF',
  volume: '#FFF',
  scrubberThumb: '#FFF',
  scrubberBar: '#FFF',
  seconds: '#FFF',
  duration: '#FFF',
  progress: '#FFF',
  loading: '#FFF'
}

class Video extends Component {
  constructor(props) {
    super(props)
    this.state = {
      paused: false,
      muted: false,
      fullScreen: false,
      inlineHeight: Win.width * 0.5625,
      loading: false,
      duration: 0,
      progress: 0,
      currentTime: 0,
      seeking: false,
      renderError: false
    }
    this.animInline = new Animated.Value(Win.width * 0.5625)
    this.animFullscreen = new Animated.Value(Win.width * 0.5625)
    this.BackHandler = this.BackHandler.bind(this)
    this.onRotated = this.onRotated.bind(this)
  }

  componentDidMount() {
    Dimensions.addEventListener('change', this.onRotated)
    BackHandler.addEventListener('hardwareBackPress', this.BackHandler)
  }

  componentWillUnmount() {
    Dimensions.removeEventListener('change', this.onRotated)
    BackHandler.removeEventListener('hardwareBackPress', this.BackHandler)
    //this.player._onStopped()
  }

  onBuffering(){
    //this.setState({ loading: true })
  }

  onLoad(data) {
    console.log('onplaying called')
  }

  onLoadProgress(event) {
    const { currentTime, duration } = event
    //if (currentTime > 0 || this.state.duration > 0) {
    if (currentTime > 0) {
      if(this.state.loading){
        //add timeout to seek to first frame of the video
        setTimeout(() => {
          this.setState({ loading: false, duration: duration })
          this.pause()
        }, 1500)
      }
    }else{
      if(!this.state.loading){
        this.setState({ loading: true })
      }
    }
  }

  progress(event) {
    const { currentTime } = event
    const progress = currentTime / this.state.duration
    if (!this.state.seeking) {
      this.setState({ progress, currentTime }, () => {
        this.props.onProgress(event)
      })
    }
  }

  onEnd(e) {
    this.props.onEnd(e)
    const { loop } = this.props
    if (!loop) this.pause()
    this.onSeekRelease(0)
    this.setState({ currentTime: 0, loading: false }, () => {
      if (!loop) this.controls.showControls()
    })
  }

  onRotated({ window: { width, height } }) {
    // Add this condition incase if inline and fullscreen options are turned on
    if (this.props.inlineOnly) return
    const orientation = width > height ? 'LANDSCAPE' : 'PORTRAIT'
    if (this.props.rotateToFullScreen) {
      if (orientation === 'LANDSCAPE') {
        this.setState({ fullScreen: true }, () => {
          this.animToFullscreen(height)
          this.props.onFullScreen(this.state.fullScreen)
        })
        return
      }
      if (orientation === 'PORTRAIT') {
        this.setState({
          fullScreen: false,
          paused: this.props.fullScreenOnly || this.state.paused
        }, () => {
          this.animToInline()
          if (this.props.fullScreenOnly) this.props.onPlay(!this.state.paused)
          this.props.onFullScreen(this.state.fullScreen)
        })
        return
      }
    } else {
      this.animToInline()
    }
    if (this.state.fullScreen) this.animToFullscreen(height)
  }

  onSeekRelease(percent) {
    const seconds = percent * this.state.duration
    this.setState({ progress: percent, seeking: false }, () => {
      this.player.seek(seconds)
    })
  }

  onError(msg) {
    //this.props.onError(msg)
    const { error } = this.props
    this.setState({ renderError: true, loading: false, paused: true }, () => {
      let type
      switch (true) {
        case error === false:
          type = error
          break
        case typeof error === 'object':
          type = Alert.alert(error.title, error.message, error.button, error.options)
          break
        default:
          type = Alert.alert('Oops!', 'There was an error playing this video, please try again later.', [{ text: 'Close' }])
          break
      }
      return type
    })
  }

  BackHandler() {
    if (this.state.fullScreen) {
      this.setState({ fullScreen: false }, () => {
        this.animToInline()
        this.props.onFullScreen(this.state.fullScreen)
        if (this.props.fullScreenOnly && !this.state.paused) this.togglePlay()
        if (this.props.rotateToFullScreen) Orientation.lockToPortrait()
        setTimeout(() => {
          if (!this.props.lockPortraitOnFsExit) Orientation.unlockAllOrientations()
        }, 1500)
      })
      return true
    }
    return false
  }

  pause() {
    if (!this.state.paused) this.togglePlay()
  }

  play() {
    if (this.state.paused) this.togglePlay()
  }

  togglePlay() {
    this.setState({ paused: !this.state.paused }, () => {
      this.props.onPlay(!this.state.paused)
      Orientation.getOrientation((e, orientation) => {
        if (this.props.inlineOnly) return
        if (!this.state.paused) {
          if (this.props.fullScreenOnly && !this.state.fullScreen) {
            this.setState({ fullScreen: true }, () => {
              this.props.onFullScreen(this.state.fullScreen)
              const initialOrient = Orientation.getInitialOrientation()
              const height = orientation !== initialOrient ?
                Win.width : Win.height
              this.animToFullscreen(height)
              if (this.props.rotateToFullScreen) Orientation.lockToLandscape()
            })
          }
        } else {
        }
      })
    })
  }

  toggleFS() {
    this.setState({ fullScreen: !this.state.fullScreen }, () => {
      Orientation.getOrientation((e, orientation) => {
        if (this.state.fullScreen) {
          const initialOrient = Orientation.getInitialOrientation()
          const height = orientation !== initialOrient ?
            Win.width : Win.height
          this.props.onFullScreen(this.state.fullScreen)
          if (this.props.rotateToFullScreen) Orientation.lockToLandscape()
          this.animToFullscreen(height)
        } else {
          if (this.props.fullScreenOnly) {
            this.setState({ paused: true }, () => this.props.onPlay(!this.state.paused))
          }
          this.props.onFullScreen(this.state.fullScreen)
          if (this.props.rotateToFullScreen) Orientation.lockToPortrait()
          this.animToInline()
          setTimeout(() => {
            if (!this.props.lockPortraitOnFsExit) Orientation.unlockAllOrientations()
          }, 1500)
        }
      })
    })
  }

  animToFullscreen(height) {
    Animated.parallel([
      Animated.timing(this.animFullscreen, { toValue: height, duration: 200 }),
      Animated.timing(this.animInline, { toValue: height, duration: 200 })
    ]).start()
  }

  animToInline(height) {
    const newHeight = height || this.state.inlineHeight
    Animated.parallel([
      Animated.timing(this.animFullscreen, { toValue: newHeight, duration: 100 }),
      Animated.timing(this.animInline, { toValue: this.state.inlineHeight, duration: 100 })
    ]).start()
  }

  toggleMute() {
    this.setState({ muted: !this.state.muted })
  }

  seek(percent) {
    const currentTime = percent * this.state.duration
    this.setState({ seeking: true, currentTime })
  }

  seekTo(seconds) {
    const percent = seconds / this.state.duration
    if (seconds > this.state.duration) {
      throw new Error(`Current time (${seconds}) exceeded the duration ${this.state.duration}`)
      return false
    }
    return this.onSeekRelease(percent)
  }

  renderError() {
    const { fullScreen } = this.state
    const inline = {
      height: this.animInline,
      alignSelf: 'stretch'
    }
    const textStyle = { color: 'white', padding: 10 }
    return (
      <Animated.View
        style={[styles.background, fullScreen ? styles.fullScreen : inline]}
      >
        <Text style={textStyle}>Retry</Text>
        <Icons
          name="replay"
          size={60}
          color='white'
          onPress={() => this.setState({ renderError: false })}
        />
      </Animated.View>
    )
  }

  renderPlayer() {
    const {
      fullScreen,
      paused,
      muted,
      loading,
      progress,
      duration,
      inlineHeight,
      currentTime
    } = this.state

    const {
      url,
      loop,
      title,
      logo,
      rate,
      style,
      volume,
      placeholder,
      theme,
      onTimedMetadata,
      resizeMode,
      onMorePress,
      inlineOnly,
      playInBackground,
      playWhenInactive,
      controlDuration,
      hideFullScreenControl,
      hideScrubber,
      hideTime,
      monitoring
    } = this.props

    const inline = {
      height: inlineHeight,
      alignSelf: 'stretch'
    }

    const setTheme = {
      ...defaultTheme,
      ...theme
    }

    console.log('inside render vlcplayer')
    return (
      <Animated.View
        style={[
          styles.background,
          fullScreen ?
            (styles.fullScreen, { height: this.animFullscreen })
            : { height: this.animInline },
          fullScreen ? null : style
        ]}
      >
        <StatusBar hidden={fullScreen} />
        {
          ((loading && placeholder) || currentTime < 0.01) &&
          <Image resizeMode="cover" style={styles.image} {...checkSource(placeholder)} />
        }
         <VLCPlayer
           {...this.props}
           ref={(ref) => { this.player = ref }}
           style={fullScreen ? styles.fullScreen : inline}
           paused={paused}
           source={{ uri: url}}
           autoplay={true}
           onProgress={(event) => this.onLoadProgress(event)}
           onEnd={(e) => this.onEnd(e)}
           onBuffering={(event) => this.onBuffering(event)} 
           onError={e => this.onError(e)}
           onStopped={(e) => this.onEnd(e)}
       />
        <Controls
          ref={(ref) => { this.controls = ref }}
          toggleMute={() => this.toggleMute()}
          toggleFS={() => this.toggleFS()}
          togglePlay={() => this.togglePlay()}
          paused={paused}
          muted={muted}
          fullscreen={fullScreen}
          loading={loading}
          onSeek={val => this.seek(val)}
          onSeekRelease={pos => this.onSeekRelease(pos)}
          progress={progress}
          currentTime={currentTime}
          duration={duration}
          logo={logo}
          monitoring={monitoring}
          title={title}
          more={!!onMorePress}
          onMorePress={() => onMorePress()}
          theme={setTheme}
          inlineOnly={inlineOnly}
          controlDuration={controlDuration}
          hideFullScreenControl={hideFullScreenControl}
          hideScrubber={hideScrubber}
          hideTime={hideTime}
        />
      </Animated.View>
    )
  }

  render() {
    //if (this.state.renderError) return this.renderError()
    return this.renderPlayer()
  }
}

Video.propTypes = {
  url: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.number
  ]).isRequired,
  placeholder: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.number
  ]),
  style: PropTypes.oneOfType([
    PropTypes.object,
    PropTypes.number
  ]),
  error: PropTypes.oneOfType([
    PropTypes.bool,
    PropTypes.object
  ]),
  loop: PropTypes.bool,
  autoPlay: PropTypes.bool,
  inlineOnly: PropTypes.bool,
  hideFullScreenControl: PropTypes.bool,
  fullScreenOnly: PropTypes.bool,
  playInBackground: PropTypes.bool,
  playWhenInactive: PropTypes.bool,
  rotateToFullScreen: PropTypes.bool,
  lockPortraitOnFsExit: PropTypes.bool,
  onEnd: PropTypes.func,
  onLoad: PropTypes.func,
  onPlay: PropTypes.func,
  onError: PropTypes.func,
  onProgress: PropTypes.func,
  onMorePress: PropTypes.func,
  onFullScreen: PropTypes.func,
  onTimedMetadata: PropTypes.func,
  rate: PropTypes.number,
  volume: PropTypes.number,
  lockRatio: PropTypes.number,
  logo: PropTypes.string,
  title: PropTypes.string,
  theme: PropTypes.object,
  resizeMode: PropTypes.string,
  controlDuration: PropTypes.number,
}

Video.defaultProps = {
  placeholder: undefined,
  style: {},
  error: true,
  loop: false,
  autoPlay: false,
  inlineOnly: false,
  fullScreenOnly: false,
  playInBackground: false,
  playWhenInactive: false,
  rotateToFullScreen: false,
  lockPortraitOnFsExit: false,
  onEnd: () => {},
  onLoad: () => {},
  onPlay: () => {},
  onError: () => {},
  onProgress: () => {},
  onMorePress: undefined,
  onFullScreen: () => {},
  onTimedMetadata: () => {},
  rate: 1,
  volume: 1,
  lockRatio: undefined,
  logo: undefined,
  title: '',
  theme: defaultTheme,
  resizeMode: 'contain',
  controlDuration: 3,
}

export default Video
