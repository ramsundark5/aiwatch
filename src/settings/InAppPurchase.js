import React, { Component } from 'react';
import { InteractionManager, StyleSheet, View } from 'react-native';
import { Button, List, Text } from 'react-native-paper';
import RNIap, {
    purchaseUpdatedListener,
    purchaseErrorListener,
    acknowledgePurchaseAndroid
} from 'react-native-iap';
import Logger from '../common/Logger';

const NO_ADS_SKU = 'com.aiwatch.noads';
let purchaseUpdateSubscription;
let purchaseErrorSubscription;
export default class InAppPurchase extends Component{

    componentDidMount() {
        InteractionManager.runAfterInteractions(() => {
            this.init();
        });
    }

    UNSAFE_componentWillUnmount() {
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
            Logger.log('IAP initialized ', result);
            //you have to first fetch products before buying
            const availableProducts = await RNIap.getProducts([NO_ADS_SKU]);
            Logger.log('availableProducts ', availableProducts);
            this.loadPurchases();
          } catch (err) {
            console.warn(err.code, err.message);
        }
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
              Logger.log('purchase ackResult', ackResult);
              this.loadPurchases();
            } catch (ackErr) {
              Logger.warn('purchase ackErr', ackErr);
            }
        }
    }

    async loadPurchases(){
        const { updateSettings } = this.props;
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

    render(){
        const { settings } = this.props;
        let isNoAdsPurchased = settings.isNoAdsPurchased;
        if(isNoAdsPurchased){
            return(
                <View style={{marginTop: 10}}>
                    <Text style={{paddingLeft: 10}}> Current License: <Text style={{fontWeight: 'bold'}}>Premium</Text></Text>
                </View>
            )
        }
        return(
            <View style={styles.premiumStyle}>
                <Button onPress={() => this.buyProduct(NO_ADS_SKU)}>Go Premium $3.99</Button>
                {this.renderFeatureText('Remove ads')}
                {this.renderFeatureText('Google drive storage')}
                {this.renderFeatureText('Remote notifications')}
                {this.renderFeatureText('Face recognition (coming soon)')}
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