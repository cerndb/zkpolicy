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
install -d $RPM_BUILD_ROOT%{_bindir}
install -d $RPM_BUILD_ROOT/etc/bash_completion.d
install -d $RPM_BUILD_ROOT%{_mandir}/man1
install -d $RPM_BUILD_ROOT%{install_path}/conf

install %{name}-%{version}-uber.jar $RPM_BUILD_ROOT%{install_path}/%{name}.jar

# Wrapper script for jar file
install bin/zkpolicy $RPM_BUILD_ROOT%{_bindir}

# Autocomplete script
install zkpolicy_autocomplete $RPM_BUILD_ROOT/etc/bash_completion.d

# Manpages
install manpages/* $RPM_BUILD_ROOT%{_mandir}/man1

# Default config files
install conf/* $RPM_BUILD_ROOT%{install_path}/conf

%files
%config(noreplace) %dir %attr(0755, root, root) %{install_path}
%attr(0755, root, root) %{install_path}/%{name}.jar
%{_bindir}/zkpolicy
/etc/bash_completion.d/zkpolicy_autocomplete
%{_mandir}/man1/zkpolicy*
%{install_path}/conf/*

%clean
rm -rf $RPM_BUILD_ROOT

%post -p /bin/sh

%preun -p /bin/sh

%postun -p /bin/sh

%changelog
* Mon Apr 03 2020 Emil Kleszcz <emil.kleszcz@cern.ch> 1.0.0 
- The first release of the ZK Policy Audit tool