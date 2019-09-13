import React, { Component } from 'react';
import {StyleSheet, Text, TouchableOpacity, View} from 'react-native';
import { Caption, Colors, IconButton, TextInput } from 'react-native-paper';
import HorizontalRow from './HorizontalRow';

export default class EditableText extends Component {
    constructor(props){
        super(props);
        this.state = {
            editableText: props.textContent,
            isEditing: props.isEditing || false
        };
    }

    _showEditMode(){
        if(this.props.editable){
            this.setState({isEditing: true});
        }
    }

    _cancelEditText(){
        this.setState({isEditing: false});
        if(this.props.cancelEditText){
            this.props.cancelEditText();
        }
    }

    _finishEditText(){
        this.props.finishEditText(this.state.editableText);
        this.setState({isEditing: false});
    }

    _onPress(){
        if(this.props.onPress){
            this.props.onPress();
        }
    }

    render(){
        const {isEditing} = this.state;
        if(isEditing){
            return this._renderEditMode();
        }
        return this._renderViewMode();
    }

    _renderViewMode(){
        const {label, mask} = this.props;
        const { editableText } = this.state;
        let maskedTextContent = editableText;
        if(mask && editableText && editableText.length >= 10){
            let lastFive = editableText.substr(editableText.length - 5); 
            maskedTextContent = 'xxxxxxxx'+lastFive;
        }
        return(
            <TouchableOpacity style={[styles.inputContainer, this.props.viewInputContainerStyle, {paddingBottom: 0, paddingTop: 0}]}>
                <HorizontalRow style={[styles.editButtonContainer, this.props.editButtonContainerStyle]}>
                    <Caption style={[styles.viewText, this.props.viewTextStyle]}>{label}: </Caption>
                    <Text style={[styles.viewText, this.props.viewTextStyle]}>{maskedTextContent}</Text>
                    {this.renderEditIcon()}
                </HorizontalRow>
            </TouchableOpacity>
        );
    }

	_renderEditMode() {
        const { editableText } = this.state;
		return (
			<View style={[styles.inputContainer, this.props.editInputContainerStyle]}>
                <HorizontalRow style={[styles.editButtonContainer, this.props.editButtonContainerStyle]}>
                    <TextInput
                        {...this.props}
                        ref='editCardInput'
                        style={[styles.editText, this.props.editInputStyle]}
                        value={editableText}
                        autoFocus={true}
                        onSubmitEditing={() => this._finishEditText()}
                        returnKeyType='done'
                        onChangeText={(changedText) => this.setState({editableText: changedText})}/>
                </HorizontalRow>  
                {this.renderApplyAndCancelEditIcons()}
            </View>
		);
    }
    
    renderEditIcon(){
        return(
            <IconButton icon='pencil-circle-outline' color={Colors.blue500} size={25} style={[styles.iconStyle, this.props.cancelIconStyle]}
                onPress={() => this._showEditMode()}/>
        )
    }

    renderApplyAndCancelEditIcons(){
        const {isEditing} = this.state;
        if(!isEditing){
            return null;
        }
        return(
            <HorizontalRow style={[styles.editButtonContainer, this.props.editButtonContainerStyle]}>
                <IconButton icon='close-circle-outline' color={Colors.red500} size={25} style={[styles.iconStyle, this.props.cancelIconStyle]}
                    onPress={() => this._cancelEditText()}/>
                <View style={styles.dummySpace}></View>
                <IconButton icon='check-circle-outline' color={Colors.green500} size={25} style={[styles.iconStyle, this.props.okayIconStyle]}
                    onPress={() => this._finishEditText()}/>
            </HorizontalRow>
        )
    }
}

const styles = StyleSheet.create({
    viewText: {
        fontSize: 14,
        color: 'black',
    },
    editText:{
        width: '100%',
        backgroundColor: 'white',
    },
    inputContainer:{
        justifyContent: 'center',
        paddingLeft: 20,
        paddingRight: 20
    },
    editButtonContainer:{
        alignItems: 'center'
    },
    iconStyle:{
        fontWeight: 'bold',
    },
    dummySpace:{
        margin: 10
    }
});