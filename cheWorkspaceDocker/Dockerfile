FROM codenvy/jdk8_maven3_tomcat8

RUN sudo apt-get update
RUN sudo apt-get -y install curl procps wget
RUN sudo apt-get update
RUN sudo apt-get install -y rabbitmq-server

RUN sudo apt-get install -y nodejs-legacy npm

USER user

WORKDIR /home/user/
RUN wget https://github.com/rnavagamuwa/flux/archive/master.zip
RUN unzip master.zip

WORKDIR /home/user/flux-master/node.server
RUN sudo npm install

ENV VCAP_APP_HOST 0.0.0.0
WORKDIR /home/user/flux-master
RUN wget https://gist.githubusercontent.com/sunix/a9a1037e257da5d0a600/raw/3ff8a910f3400d19775bb562c89524518ac6f2d2/startup-all-in-one.js.patch
RUN patch -p1 < startup-all-in-one.js.patch
WORKDIR /home/user/flux-master/node.server

CMD sudo service rabbitmq-server start && npm start