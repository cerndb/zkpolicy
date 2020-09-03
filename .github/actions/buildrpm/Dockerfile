# Using CentOS 7 as base image to support rpmbuild (packages will be Dist el7)
FROM centos:7

# Setting Maven Version that needs to be installed
ARG MAVEN_VERSION=3.6.0

# Copying all contents of rpmbuild repo inside container
COPY . .

# Installing tools needed for rpmbuild,
# depends on BuildRequires field in specfile.
RUN yum install -y rpm-build rpmdevtools gcc make coreutils python

RUN yum -y install \
java-1.8.0-openjdk \
java-1.8.0-openjdk-devel \
which

# Maven
RUN curl -fsSL https://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz | tar xzf - -C /usr/share \
  && mv /usr/share/apache-maven-$MAVEN_VERSION /usr/share/maven \
  && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

ENV MAVEN_VERSION=${MAVEN_VERSION}
ENV M2_HOME /usr/share/maven
ENV maven.home $M2_HOME
ENV M2 $M2_HOME/bin
ENV PATH $M2:$PATH

RUN echo "JAVA_HOME=$(readlink -f /usr/bin/java | sed "s:bin/java::")" | tee -a /etc/profile && source /etc/profile && echo $JAVA_HOME

RUN mkdir /build
WORKDIR /build

# Code file to execute when the docker container starts up (`entrypoint.sh`)
ENTRYPOINT ["./.github/actions/buildrpm/entrypoint.sh"]
