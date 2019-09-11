import React, { Component } from 'react';
import {View} from 'react-native';

export default class HorizontalRow extends Component {
	render() {
		return (
			<View style={[{ flexDirection: 'row' }, this.props.style ]}>
				{ this.props.children }
			</View>
		);
	}
}