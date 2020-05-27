import React from "react";
import { TouchableOpacity, View, Text, Image } from "react-native";
import RNSlider from "react-native-slider";
import styles from "./MediaControls.style";
import { humanizeVideoDuration } from "./utils";
import { PLAYER_STATES } from "./constants/playerStates";

const fullScreenImage = require("./assets/ic_fullscreen.png");

const Slider = props => {
  const { progress, duration, mainColor, onFullScreen, onPause } = props;

  const dragging = (value) => {
    const { onSeeking, playerState } = props;
    onSeeking(value);

    if (playerState === PLAYER_STATES.PAUSED) {
      return;
    }

    onPause();
  };

  const seekVideo = (value) => {
    props.onSeek(value);
    onPause();
  };

  return (
    <View style={[styles.controlsRow, styles.progressContainer]}>
      <View style={styles.progressColumnContainer}>
        <View style={[styles.timerLabelsContainer]}>
          <Text style={styles.timerLabel}>
            {humanizeVideoDuration(progress)}
          </Text>
          <Text style={styles.timerLabel}>
            {humanizeVideoDuration(duration)}
          </Text>
        </View>
        <RNSlider
          style={styles.progressSlider}
          onValueChange={dragging}
          onSlidingComplete={seekVideo}
          maximumValue={Math.floor(duration)}
          value={Math.floor(progress)}
          trackStyle={styles.track}
          thumbStyle={[styles.thumb, { borderColor: mainColor }]}
          minimumTrackTintColor={mainColor}
        />
      </View>
      {Boolean(onFullScreen) && (
        <TouchableOpacity
          style={styles.fullScreenContainer}
          onPress={onFullScreen}
        >
          <Image source={fullScreenImage} />
        </TouchableOpacity>
      )}
    </View>
  );
};

export { Slider };
