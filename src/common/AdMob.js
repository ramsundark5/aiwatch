import Logger from './Logger';
import { AdMobInterstitial } from 'expo-ads-admob';
import Config from 'react-native-config';

class AdMob{
    async showAd(){
        try{
            let isRemoveAds = Config.REMOVE_ADS;
            if(isRemoveAds){
              return;  
            }
            let AD_UNIT_ID = Config.AD_UNIT_ID;
            AdMobInterstitial.setAdUnitID(AD_UNIT_ID); 
            //AdMobInterstitial.setTestDeviceID('EMULATOR');
            await AdMobInterstitial.requestAdAsync();
            await AdMobInterstitial.showAdAsync();
          }catch(err){
            Logger.error(err);
          }
    }
}

export default new AdMob();