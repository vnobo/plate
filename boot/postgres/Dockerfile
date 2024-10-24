FROM postgres:latest

# 设置时区
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
RUN localedef -i zh_CN -c -f UTF-8 -A /usr/share/locale/locale.alias zh_CN.UTF-8
RUN localedef -i zh_HK -c -f UTF-8 -A /usr/share/locale/locale.alias zh_HK.UTF-8
RUN localedef -i zh_TW -c -f UTF-8 -A /usr/share/locale/locale.alias zh_TW.UTF-8 
ENV LANG=zh_CN.utf8
