FROM node:5

RUN mkdir -p /usr/src/app

WORKDIR /usr/src/app

ADD package.json package.json
RUN npm install

ADD scripts scripts
ADD api api
ADD gateway gateway
RUN rm -rf gateway/services.json
RUN cp gateway/services_docker.json gateway/services.json
ADD views views
ADD .babelConfig.js .babelConfig.js
ADD .babelrc .babelrc
ADD config.js config.js

EXPOSE 8080 3030

CMD ["npm", "start"]
