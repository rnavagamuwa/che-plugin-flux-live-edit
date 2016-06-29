FROM codenvy/jdk8_maven3_tomcat8
RUN apt-get update && apt-get -y install curl sudo procps wget && \
    echo "%sudo ALL=(ALL) NOPASSWD: ALL" >> /etc/sudoers && \
    useradd -u 1000 -G users,sudo -d /home/user --shell /bin/bash -m user && \
    echo "secret\nsecret" | passwd user && \
    curl -sSL https://get.docker.com/ | sh && \
    usermod -aG docker user && sudo apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*
