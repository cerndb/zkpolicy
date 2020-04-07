#
# SPEC file for zkpolicy tool
#

%define version     1.0.0
%define name        cerndb-sw-zkpolicy
%define release     %{_release}%{?dist}
%define repodir     %{name}-%{version}

%define install_path      /opt/zkpolicy

%define __jar_repack %{nil}

BuildRoot:     %{_builddir}/%{name}-build
BuildArch:     noarch

Name:          %{name}
Version:       %{version}
Release:       %{release}
Prefix:        %{install_path}
Source:        %{name}-%{version}.tar.gz
License:       GPL
Group:         System
Summary:       The Zookeeper Policy Audit Tool

URL:           https://gitlab.cern.ch/db/zookeeper-policy-audit-tool.git

Provides:      %{name} = %{version}-%{release}
Provides:      %{name}
Requires:      /bin/bash
Requires:      /bin/sh
Requires:      java >= 1.8

%description
Zookeeper audit and policy tool for checking and enforcing ACLs on the znodes.

%prep
%setup -q

%install
%{__rm} -rf $RPM_BUILD_ROOT/*
install -d $RPM_BUILD_ROOT%{install_path}
install %{name}-%{version}.jar $RPM_BUILD_ROOT%{install_path}/%{name}-%{version}.jar

mkdir -p $RPM_BUILD_ROOT%{install_path}

%files
%config(noreplace) %dir %attr(0755, root, root) %{install_path}
%attr(0755, root, root) %{install_path}/%{name}-%{version}.jar

ln -sf %{install_path}/%{name}-%{version}.jar $RPM_BUILD_ROOT/usr/bin/

%clean
rm -rf $RPM_BUILD_ROOT

%post -p /bin/sh

%preun -p /bin/sh

%postun -p /bin/sh

%changelog
* Mon Apr 03 202O Emil Kleszcz <emil.kleszcz@cern.ch>
- 1.0.0 The first release of the ZK Policy Audit tool