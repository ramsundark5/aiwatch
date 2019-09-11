import React, { Component } from 'react';
import {StyleSheet, Text, TouchableOpacity, View} from 'react-native';
import { IconButton, TextInput } from 'react-native-paper';
import HorizontalRow from './HorizontalRow';

export default class EditableText extends Component {
    constructor(props){
        super(props);
        this.state = {
            editableText: props.textContent,
            isEditing: props.isEditing || false
        };
    }

    _showEditMode(text){
        if(this.props.editable){
            this.setState({editableText: text, isEditing: true});
        }
    }

    _cancelEditText(){
        this.setState({editableText: '', isEditing: false});
        if(this.props.cancelEditText){
            this.props.cancelEditText();
        }
    }

    _finishEditText(){
        this.props.finishEditText(this.state.editableText);
        this.setState({editableText: '', isEditing: false});
    }

    render(){
        const {isEditing} = this.state;
        if(isEditing){
            return this._renderEditMode();
        }
        else{
            return this._renderViewMode();
        } 
    }

    _onPress(){
        if(this.props.onPress){
            this.props.onPress();
        }
    }
    
    _renderViewMode(){
        const {textContent} = this.props;
        return(
            <TouchableOpacity style={[styles.inputContainer, this.props.viewInputContainerStyle]} 
                    onPress={() => this._onPress()}
                    onLongPress={() => this._showEditMode(textContent)}>
                <Text style={[styles.viewText, this.props.viewTextStyle]}>{textContent}</Text>
            </TouchableOpacity>
        );
    }

	_renderEditMode() {
		return (
			<View style={[styles.inputContainer, this.props.editInputContainerStyle]}>
                <TextInput
                        ref='editCardInput'
                        style={[styles.editInput, this.props.editInputStyle]}
                        value={this.state.editableText}
                        autoFocus={true}
                        onSubmitEditing={() => this._finishEditText()}
                        returnKeyType='done'
                        onChangeText={(changedText) => this.setState({editableText: changedText})}/>
                <HorizontalRow style={[styles.editButtonContainer, this.props.editButtonContainerStyle]}>
                    <IconButton icon='ios-close-circle-outline' style={[styles.cancelIcon, this.props.cancelIconStyle]}
                        onPress={() => this._cancelEditText()}/>
                    <View style={styles.dummySpace}></View>
                    <IconButton icon='ios-checkmark-circle-outline' style={[styles.okayIcon, this.props.okayIconStyle]}
                        onPress={() => this._finishEditText()}/>
                </HorizontalRow>
            </View>
		);
	}
}

const styles = StyleSheet.create({
    viewText: {
        fontSize: 14,
        color: 'white'
    },
    inputContainer:{
        justifyContent: 'center',
    },
    editInput: {
        height  : 26,
        fontSize: 14,
        color: 'white',
        fontWeight: 'bold',
        marginLeft: 10,
        marginRight: 10,
        textAlign: 'center',
        flex: 1
    },
    editButtonContainer:{
        marginTop: 10,
        alignItems: 'center'
    },
    cancelIcon:{
        fontWeight: 'bold',
        fontSize : 40,
        color: 'red'
    },
    okayIcon:{
        fontWeight: 'bold',
        fontSize : 40,
        color: 'green'
    },
    dummySpace:{
        margin: 10
    }
});