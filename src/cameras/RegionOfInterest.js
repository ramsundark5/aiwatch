import CustomCrop from '../common/CustomCrop';
import React, { Component } from 'react';
import { View, Image, TouchableOpacity, Text } from 'react-native';

export default class RegionOfInterest extends Component{

    state = {
        
    }

    componentWillMount() {
      this.init();
    }
    
    init(){
      try{
        //const imageUri = 'https://i.pinimg.com/originals/39/42/a1/3942a180299d5b9587c2aa8e09d91ecf.jpg';
        console.log('loading sample image');
        const url = require('./inputsample.jpg');
        const image = Image.resolveAssetSource(url);
        console.log('loaded sample image' + image);
        //Image.getSize(image.uri, (width, height) => {
          this.setState({
            imageWidth: image.width,
            imageHeight: image.height,
            initialImage: image.uri,
          });
        //});
      }catch(err){
        console.log('Error loading sample image' + err);
      }
    }
    updateImage(image, newCoordinates) {
        this.setState({
          image,
          rectangleCoordinates: newCoordinates
        });
    }
    
    crop() {
        //this.customCrop.crop();
    }
    
    render() {
        return (
          <View>
            <CustomCrop
              updateImage={this.updateImage.bind(this)}
              rectangleCoordinates={this.state.rectangleCoordinates}
              initialImage={this.state.initialImage}
              height={this.state.imageHeight}
              width={this.state.imageWidth}
              ref={ref => (this.customCrop = ref)}
              overlayColor="rgba(18,190,210, 1)"
              overlayStrokeColor="rgba(20,190,210, 1)"
              handlerColor="rgba(20,150,160, 1)"
              enablePanStrict={false}
            />
          </View>
        );
    }
}