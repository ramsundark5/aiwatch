import * as RNIap from 'react-native-iap';
import React, { Component } from 'react';
import { Button } from 'react-native-paper';
import { View } from 'react-native';
let purchaseUpdateSubscription;
let purchaseErrorSubscription;

export default class InAppPurchase extends Component{

    state = {
        receipt: '',
        availableItemsMessage: '',
    };

    componentDidMount() {
      if(!this.props.isNoAdsPurchased){
        this.init();
      }
    }

    async init() {
        try {
          const result = await RNIap.initConnection();
          await RNIap.consumeAllItemsAndroid();
          console.log('result', result);
        } catch (err) {
          console.warn(err.code, err.message);
        }
    
        purchaseUpdateSubscription = purchaseUpdatedListener((purchase) => {
          console.log('purchaseUpdatedListener', purchase);
          //this.setState({ receipt: purchase.transactionReceipt }, () => this.goNext());
        });
    
        purchaseErrorSubscription = purchaseErrorListener((error) => {
          console.log('purchaseErrorListener', error);
          //Alert.alert('purchase error', JSON.stringify(error));
        });
    }
    
    componentWillMount() {
        if (purchaseUpdateSubscription) {
          purchaseUpdateSubscription.remove();
          purchaseUpdateSubscription = null;
        }
        if (purchaseErrorSubscription) {
          purchaseErrorSubscription.remove();
          purchaseErrorSubscription = null;
        }
    }

    getAvailablePurchases = async() => {
        try {
          console.info('Get available purchases (non-consumable or unconsumed consumable)');
          const purchases = await RNIap.getAvailablePurchases();
          console.info('Available purchases :: ', purchases);
          if (purchases && purchases.length > 0) {
            this.setState({
              availableItemsMessage: `Got ${purchases.length} items.`,
              receipt: purchases[0].transactionReceipt,
            });
          }
        } catch (err) {
          console.warn(err.code, err.message);
          //Alert.alert(err.message);
        }
    }
    
    // Version 3 apis
    requestPurchase = async(sku) => {
        try {
          RNIap.requestPurchase(sku);
        } catch (err) {
          console.warn(err.code, err.message);
        }
    }

    render(){
        return(
            <View>
                <Button onPress={() => this.requestPurchase('com.aiwatch.noads')}> Purchase Pro </Button>
            </View>
        )
    }
}