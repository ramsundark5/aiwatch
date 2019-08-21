import React, { Component } from 'react';
import {
    PanResponder,
    Dimensions,
    Image,
    View,
    Animated,
} from 'react-native';
import Svg, { Polygon } from 'react-native-svg';

const AnimatedPolygon = Animated.createAnimatedComponent(Polygon);

class CustomCrop extends Component {
    constructor(props) {
        super(props);
        this.state = {
            viewHeight:
                Dimensions.get('window').width * (props.height / props.width),
            height: props.height,
            width: props.width,
            image: props.initialImage,
            moving: false,
        };

        const rect = props.rectangleCoordinates;
        const topLeft = rect && rect.topLeftX
                ? this.imageCoordinatesToViewCoordinates({x: rect.topLeftX, y: rect.topLeftY}, true)
                : { x: 100, y: 100 };
        const topRight = rect && rect.topRightX
                ? this.imageCoordinatesToViewCoordinates({x: rect.topRightX, y: rect.topRightY}, true)
                : { x: props.width - 100, y: 100 };
        const bottomLeft = rect && rect.bottomLeftX
                ? this.imageCoordinatesToViewCoordinates({x: rect.bottomLeftX, y: rect.bottomLeftY}, true)
                : { x: 100, y: props.height - 100 };
        const bottomRight = rect && rect.bottomRightX
                ? this.imageCoordinatesToViewCoordinates({x: rect.bottomRightX, y: rect.bottomRightY}, true)
                : { x: props.width - 100, y: props.height - 100};

        this.state = {
            ...this.state,
            topLeft: new Animated.ValueXY(topLeft),
            topRight: new Animated.ValueXY(topRight),
            bottomLeft: new Animated.ValueXY(bottomLeft),
            bottomRight: new Animated.ValueXY(bottomRight)
        };
        this.state = {
            ...this.state,
            overlayPositions: `${this.state.topLeft.x._value},${
                this.state.topLeft.y._value
            } ${this.state.topRight.x._value},${this.state.topRight.y._value} ${
                this.state.bottomRight.x._value
            },${this.state.bottomRight.y._value} ${
                this.state.bottomLeft.x._value
            },${this.state.bottomLeft.y._value}`,
        };

        this.panResponderTopLeft = this.createPanResponser(this.state.topLeft);
        this.panResponderTopRight = this.createPanResponser(
            this.state.topRight,
        );
        this.panResponderBottomLeft = this.createPanResponser(
            this.state.bottomLeft,
        );
        this.panResponderBottomRight = this.createPanResponser(
            this.state.bottomRight,
        );
    }

    createPanResponser(corner) {
        return PanResponder.create({
            onStartShouldSetPanResponder: () => true,
            onPanResponderMove: Animated.event([
                null,
                {
                    dx: corner.x,
                    dy: corner.y,
                },
            ]),
            onPanResponderRelease: () => {
                corner.flattenOffset();
                this.updateOverlayString();
            },
            onPanResponderGrant: () => {
                corner.setOffset({ x: corner.x._value, y: corner.y._value });
                corner.setValue({ x: 0, y: 0 });
            },
        });
    }

    selectROI() {
        const coordinates = {
            topLeft: this.viewCoordinatesToImageCoordinates(this.state.topLeft),
            topRight: this.viewCoordinatesToImageCoordinates(
                this.state.topRight,
            ),
            bottomLeft: this.viewCoordinatesToImageCoordinates(
                this.state.bottomLeft,
            ),
            bottomRight: this.viewCoordinatesToImageCoordinates(
                this.state.bottomRight,
            ),
            height: this.state.height,
            width: this.state.width,
        };
        return coordinates;
    }

    updateOverlayString() {
        this.setState({
            overlayPositions: `${this.state.topLeft.x._value},${
                this.state.topLeft.y._value
            } ${this.state.topRight.x._value},${this.state.topRight.y._value} ${
                this.state.bottomRight.x._value
            },${this.state.bottomRight.y._value} ${
                this.state.bottomLeft.x._value
            },${this.state.bottomLeft.y._value}`,
        });
    }

    imageCoordinatesToViewCoordinates(corner) {
        return {
            x: (corner.x * this.props.width) / this.state.width,
            y: (corner.y * this.props.height) / this.state.height,
        };
    }

    viewCoordinatesToImageCoordinates(corner) {
        return {
            x:
                (corner.x._value / this.props.width) *
                this.state.width,
            y: (corner.y._value / this.props.height) * this.state.height,
        };
    }

    render() {
        return (
            <View style={styles(this.props).container}>
                <View
                    style={[styles(this.props).cropContainer,]}>
                    <Image
                        style={[styles(this.props).image]}
                        resizeMode="contain"
                        source={{ uri: this.state.image }}/>
                    <Svg
                        height={this.state.viewHeight}
                        width={Dimensions.get('window').width}
                        style={{ position: 'absolute', left: 0, top: 0 }}>
                        <AnimatedPolygon
                            ref={(ref) => (this.polygon = ref)}
                            fill={this.props.overlayColor || 'blue'}
                            fillOpacity={this.props.overlayOpacity || 0.5}
                            stroke={this.props.overlayStrokeColor || 'blue'}
                            points={this.state.overlayPositions}
                            strokeWidth={this.props.overlayStrokeWidth || 3}/>
                    </Svg>
                    <Animated.View
                        {...this.panResponderTopLeft.panHandlers}
                        style={[
                            this.state.topLeft.getLayout(),
                            styles(this.props).handler,
                        ]}>
                        <View style={styles(this.props).handlerRound}/>
                    </Animated.View>
                    <Animated.View
                        {...this.panResponderTopRight.panHandlers}
                        style={[
                            this.state.topRight.getLayout(),
                            styles(this.props).handler,
                        ]}>
                        <View style={styles(this.props).handlerRound}/>
                    </Animated.View>
                    <Animated.View
                        {...this.panResponderBottomLeft.panHandlers}
                        style={[
                            this.state.bottomLeft.getLayout(),
                            styles(this.props).handler,
                        ]}>
                        <View style={styles(this.props).handlerRound}/>
                    </Animated.View>
                    <Animated.View
                        {...this.panResponderBottomRight.panHandlers}
                        style={[
                            this.state.bottomRight.getLayout(),
                            styles(this.props).handler,
                        ]}>
                        <View style={styles(this.props).handlerRound}/>
                    </Animated.View>
                </View>
            </View>
        );
    }
}

const styles = (props) => ({
    container: {
        flex: 1,
        flexDirection: 'row',
        justifyContent: 'center',
        alignItems: 'center'
    },
    handlerI: {
        borderRadius: 0,
        height: 20,
        width: 20,
        backgroundColor: props.handlerColor || 'blue',
    },
    handlerRound: {
        width: 29,
        position: 'absolute',
        height: 29,
        borderRadius: 100,
        backgroundColor: props.handlerColor || 'blue',
    },
    image: {
        width: props.width,
        height: props.height,
    },
    bottomButton: {
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: 'blue',
        width: 40,
        height: 40,
        borderRadius: 100,
    },
    handler: {
        height: 140,
        width: 140,
        overflow: 'visible',
        marginLeft: -70,
        marginTop: -70,
        alignItems: 'center',
        justifyContent: 'center',
        position: 'absolute',
    },
    cropContainer: {
       
    },
});

export default CustomCrop;