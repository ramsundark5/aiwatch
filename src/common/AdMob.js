import Logger from './Logger';
import { AdMobInterstitial } from 'react-native-admob';

class AdMob{
    async showAd(isRemoveAds){
        try{
            Logger.log('AdMob isremovedAds flag value is '+isRemoveAds);
            isRemoveAds = true;
            if(isRemoveAds){
              return;  
            }
            let AD_UNIT_ID = 'ca-app-pub-3233599560396549/3036076005';
            if (__DEV__) {
              AD_UNIT_ID = 'ca-app-pub-3940256099942544/1033173712';
            }
            AdMobInterstitial.setAdUnitID(AD_UNIT_ID); 
            AdMobInterstitial.setTestDevices(['EMULATOR']);
            //await AdMobInterstitial.requestAdAsync();
            await AdMobInterstitial.requestAd();
            await AdMobInterstitial.showAd();
            Logger.log('Ad shown for '+AD_UNIT_ID);
          }catch(err){
            Logger.error(err);
          }
    }
}

export default new AdMob();