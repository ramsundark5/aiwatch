import Logger from './Logger';
import { AdMobInterstitial } from 'expo-ads-admob';

class AdMob{
    async showAd(isRemoveAds){
        try{
            Logger.log('AdMob isremovedAds flag value is '+isRemoveAds);
            if(isRemoveAds){
              return;  
            }
            let AD_UNIT_ID = 'ca-app-pub-3233599560396549/3036076005';
            if (__DEV__) {
              AD_UNIT_ID = 'ca-app-pub-3940256099942544/1033173712';
            }
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