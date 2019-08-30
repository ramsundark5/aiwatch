import { authorize } from 'react-native-app-auth';

// base config
const config = {
  serviceConfiguration:{
    authorizationEndpoint: 'https://graph.api.smartthings.com/oauth/authorize',
    tokenEndpoint: 'https://graph.api.smartthings.com/oauth/token'
  },
  clientId: '5c9baee2-daa5-46ff-abc0-2cdceeb284ea',
  clientSecret: 'f6a70c01-410d-46dd-95a0-94b7ad76f8b2',
  redirectUrl: 'com.aiwatch.oauth:/oauthredirect',
  scopes: ['app'],
};

class SmartthingsIntegration{
    async getOauthToken(){
        try {
            console.log('starting authorize');
            const result = await authorize(config);
            console.log('smartthings token '+JSON.stringify(result));
            return result;
            // result includes accessToken, accessTokenExpirationDate and refreshToken
        } catch (error) {
            console.log(error);
        }
    }
}

export default new SmartthingsIntegration();