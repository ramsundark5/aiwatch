import React, { Component } from 'react';
import { StyleSheet, View } from 'react-native';
import { Button, List, Text } from 'react-native-paper';
import RNIap, {
    purchaseUpdatedListener,
    purchaseErrorListener,
    acknowledgePurchaseAndroid
} from 'react-native-iap';
import Logger from '../common/Logger';
import BackgroundListener from '../events/BackgroundListener';

const NO_ADS_SKU = 'com.aiwatch.noads';
let purchaseUpdateSubscription;
let purchaseErrorSubscription;
export default class InAppPurchase extends Component{

    componentDidMount() {
        this.init();
    }

    componentWillMount() {
        try{
            RNIap.endConnectionAndroid();
            if (purchaseUpdateSubscription) {
              purchaseUpdateSubscription.remove();
              purchaseUpdateSubscription = null;
            }
           if (purchaseErrorSubscription) {
              purchaseErrorSubscription.remove();
              purchaseErrorSubscription = null;
            }
        }catch(err){
            Logger.error(err);
        }
    }

    async init(){
        try {
            const result = await RNIap.initConnection();
            console.log('result', result);
          } catch (err) {
            console.warn(err.code, err.message);
        }
        //const availableProducts = await RNIap.getProducts([NO_ADS_SKU]);
        //console.log('availableProducts ', availableProducts);
        purchaseUpdateSubscription = purchaseUpdatedListener((purchase) => {
            this.handlePurchaseUpdate(purchase);
        });
        purchaseErrorSubscription = purchaseErrorListener((error) => {
          this.handlePurchaseError(error);
        });
    }

    async handlePurchaseUpdate(purchase){
        if (purchase.purchaseStateAndroid === 1 && !purchase.isAcknowledgedAndroid) {
            try {
              const ackResult = await acknowledgePurchaseAndroid(purchase.purchaseToken);
              console.log('ackResult', ackResult);
              BackgroundListener.getPurchases();
            } catch (ackErr) {
              console.warn('ackErr', ackErr);
            }
        }
    }

    async getPurchases(){
        try {
            const purchases = await RNIap.getAvailablePurchases();
            purchases.forEach(purchase => {
                if (purchase.productId == NO_ADS_SKU) {
                    updateSettings({isNoAdsPurchased: true});
                } 
            });
        } catch(err) {
          console.warn(err); // standardized err.code and err.message available
        }
    }

    handlePurchaseError(error){
        Logger.log('purchaseErrorListener', error);
    }

    async buyProduct(itemSku){
        try{
            // Will return a purchase object with a receipt which can be used to validate on your server.
            const purchase = await RNIap.requestPurchase(itemSku);
            this.setState({
                receipt: purchase.transactionReceipt, // save the receipt if you need it, whether locally, or to your server.
            });
        } catch(err) {
            // standardized err.code and err.message available
            Logger.error(err.message);
        }
    }

    async getPurchases(){
        try {
            const purchases = await RNIap.getAvailablePurchases();
            purchases.forEach(purchase => {
                if (purchase.productId == NO_ADS_SKU) {
                    updateSettings({isNoAdsPurchased: true});
                } 
            });
        } catch(err) {
          console.warn(err); // standardized err.code and err.message available
        }
    }

    render(){
        const { settings } = this.props;
        let isNoAdsPurchased = settings.isNoAdsPurchased;
        if(isNoAdsPurchased){
            return(
                <View>
                    <Text>Current License: Premium</Text>
                </View>
            )
        }
        return(
            <View style={styles.premiumStyle}>
                <Button onPress={() => this.buyProduct(NO_ADS_SKU)}>Go Premium $3.99</Button>
                {this.renderFeatureText('Remove ads')}
                {this.renderFeatureText('Alexa integration (coming soon)')}
                {this.renderFeatureText('Region of interest (coming soon)')}
            </View>
        )
    }

    renderFeatureText(text){
        return(
            <Button uppercase={false} icon="check" color="black">
                {text}
            </Button>
        )
    }
}

const styles = StyleSheet.create({
    premiumStyle: {
      //textAlign: 'center',
      //alignItems: 'center'
      paddingLeft: '20%',
      justifyContent: 'space-around',
      alignItems: 'flex-start'
    },
  });