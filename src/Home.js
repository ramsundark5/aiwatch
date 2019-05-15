import React, { Component } from "react";
import {
  Container,
  Header,
  Content,
  Footer,
  FooterTab,
  Button,
  Icon,
  Text
} from "native-base";

export default class Home extends Component {
  render() {
    return (
      <Container>
        <Header />
        <Content />
        <Footer>
          <FooterTab>
            <Button vertical>
              <Icon name="videocam" />
              <Text>Cameras</Text>
            </Button>
            <Button vertical>
              <Icon active name="navigate" />
              <Text>Events</Text>
            </Button>
            <Button vertical>
              <Icon name="person" />
              <Text>Settings</Text>
            </Button>
          </FooterTab>
        </Footer>
      </Container>
    );
  }
}
