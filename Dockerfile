FROM --platform=linux/amd64 sapmachine:ubuntu-24.04

ENV JAVA_HOME=/opt/jdk
ENV PATH=$PATH:/opt/jdk/bin:/opt/maven/bin
ARG DEBIAN_FRONTEND=noninteractive
ARG TARGETARCH
ARG CANTALOUPE_VERSION="5.0.6"
ARG CANTALOUPE_FILE="cantaloupe-${CANTALOUPE_VERSION}.zip"
ARG CANTALOUPE_URL="https://github.com/cantaloupe-project/cantaloupe/releases/download/v${CANTALOUPE_VERSION}/${CANTALOUPE_FILE}"
ARG CANTALOUPE_SHA256="35311eb0d4d6f0578cab42fd5e51d6150e62821cb3b4ee3a265e2befbeeb5897"

EXPOSE 8182

WORKDIR /opt/cantaloupe

RUN apt-get update && apt-get install -y --no-install-recommends \
    wget \
    curl \
    unzip \
    ca-certificates \
    gnupg \
    && rm -rf /var/lib/apt/lists/*

RUN curl -L "https://github.com/cantaloupe-project/cantaloupe/releases/download/v${CANTALOUPE_VERSION}/cantaloupe-${CANTALOUPE_VERSION}.zip" > /tmp/cantaloupe.zip \ 
        && unzip /tmp/cantaloupe.zip -d /opt/cantaloupe \
        && cp /opt/cantaloupe/cantaloupe-${CANTALOUPE_VERSION}/cantaloupe-${CANTALOUPE_VERSION}.jar /opt/cantaloupe/cantaloupe.jar
# COPY cantaloupe-5.0.6/cantaloupe-${CANTALOUPE_VERSION}.jar /opt/cantaloupe/cantaloupe.jar


# https://github.com/cantaloupe-project/cantaloupe/releases/download/v5.0.6/cantaloupe-5.0.6.zip
# Platform agnostic does not require arch specific identifier.
# RUN --mount=type=cache,id=cantaloupe-downloads-${TARGETARCH},sharing=locked,target=/opt/downloads \
#     download.sh \
#         --url "${CANTALOUPE_URL}" \
#         --sha256 "${CANTALOUPE_SHA256}" \
#         --dest "/opt/cantaloupe" \
#         --strip \
#         deps \
#     && \
#     mv "/opt/cantaloupe/cantaloupe-${CANTALOUPE_VERSION}.jar" "/opt/cantaloupe/cantaloupe.jar" && \
#     cleanup.sh

# Install various dependencies:
# * ca-certificates is needed by wget
# * ffmpeg is needed by FfmpegProcessor
# * wget download stuffs in this dockerfile
# * libopenjp2-tools is needed by OpenJpegProcessor
# * All the rest is needed by GrokProcessor
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    cmake \
    git \
    dpkg \
    ffmpeg \
    nano \
    sudo \
    imagemagick \
    graphicsmagick \
    libgrokj2k1 \
    grokj2k-tools \
    maven \
    libturbojpeg \
    libopenjp2-7 \
    libopenjp2-tools \
        liblcms2-dev \
        libpng-dev \
        libzstd-dev \
        libtiff-dev \
        libz-dev \
        libjpeg-dev \
        zlib1g-dev \
        libwebp-dev \
        libimage-exiftool-perl \
    && rm -rf /var/lib/apt/lists/*

# openjpeg
# RUN git clone https://github.com/uclouvain/openjpeg.git \
#         && cd openjpeg \
#         && mkdir build \ 
#         && cd build \
#         && cmake .. -DCMAKE_BUILD_TYPE=Release \
#         && make


# Install TurboJpegProcessor dependencies
RUN mkdir -p /opt/libjpeg-turbo/lib
COPY ./processors/libjpeg-turbo/lib64 /opt/libjpeg-turbo/lib

# Install KakaduNativeProcessor dependencies
COPY ./processors/kdu/* /usr/lib/

# Install various other dependencies that aren't in apt
# Install GrokProcessor dependencies
# RUN wget http://ftp.us.debian.org/debian/pool/main/libg/libgrokj2k/libgrokj2k1_10.0.5-1+b2_amd64.deb 
# RUN sudo dpkg -i libgrokj2k1_10.0.5-1+b2_amd64.deb
# RUN sudo apt-get -f install
# RUN wget http://ftp.us.debian.org/debian/pool/main/libg/libgrokj2k/grokj2k-tools_10.0.5-1+b2_amd64.deb
# RUN sudo apt install grokj2k-tools_10.0.5-1+b2_amd64.deb
# RUN sudo apt-get -f install

# Install OPENJDK
RUN wget -q https://download.java.net/java/GA/jdk22.0.2/c9ecb94cd31b495da20a27d4581645e8/9/GPL/openjdk-22.0.2_linux-x64_bin.tar.gz \
    && tar xfz openjdk-22.0.2_linux-x64_bin.tar.gz \
    && mv jdk-22.0.2 /opt/jdk \
# INSTALL MAVEN    
    && wget -q https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.8/apache-maven-3.9.8-bin.tar.gz  \
    && tar xfz apache-maven-3.9.8-bin.tar.gz \
    && mv apache-maven-3.9.8 /opt/maven \
    && rm apache-maven-3.9.8-bin.tar.gz


# ADD TURBOJPEG APT REPO
RUN wget -q -O- https://packagecloud.io/dcommander/libjpeg-turbo/gpgkey | gpg --dearmor >/etc/apt/trusted.gpg.d/libjpeg-turbo.gpg \
        && wget -P /etc/apt/sources.list.d/ https://raw.githubusercontent.com/libjpeg-turbo/repo/main/libjpeg-turbo.list \
        && apt-get update

# TURBOJPEG INSTALL
RUN apt-get update && apt-get install -y --no-install-recommends \
     libjpeg-turbo-official \
     && rm -rf /var/lib/apt/lists/*


# A non-root user is needed for some FilesystemSourceTest tests to work.
# ARG user=cantaloupe
# ARG home=/home/$user
RUN useradd -ms /bin/bash cantaloupe
USER cantaloupe
WORKDIR /home/cantaloupe
RUN chown -R cantaloupe:cantaloupe /home/cantaloupe
# USER $user
# WORKDIR $home

# Install application dependencies
COPY ./pom.xml pom.xml
RUN mvn --quiet dependency:resolve

# Expose the port Cantaloupe will run on
EXPOSE 8182

# Set the working directory
WORKDIR /opt/cantaloupe

# 5.0.6.jar to /opt/cantaloupe
# COPY ./cantaloupe-5.0.6/cantaloupe-5.0.6.jar /opt/cantaloupe

# Copy the code
COPY --chown=cantaloupe processors/test.properties /test.properties
COPY --chown=cantaloupe src/ /src
COPY --chown=cantaloupe cantaloupe/ /opt/cantaloupe/

# Run Cantaloupe
CMD ["java", "-Dcantaloupe.config=/opt/cantaloupe/cantaloupe.properties", "-Xmx2g", "-jar", "cantaloupe.jar"]
