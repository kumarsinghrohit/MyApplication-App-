FROM openresty/openresty:alpine-fat
RUN rm /usr/local/openresty/nginx/conf/nginx.conf
COPY ./nginx.conf /usr/local/openresty/nginx/conf/
COPY ./application/* /etc/nginx/conf.d/
EXPOSE 80