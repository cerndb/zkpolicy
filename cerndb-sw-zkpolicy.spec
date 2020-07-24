#
# SPEC file for zkpolicy tool
#

%define version     1.0.1
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
install -d $RPM_BUILD_ROOT%{install_path}/conf/policies

install %{name}-%{version}-%{_release}-uber.jar $RPM_BUILD_ROOT%{install_path}/%{name}.jar

# Wrapper script for jar file
install bin/zkpolicy $RPM_BUILD_ROOT%{_bindir}

# Autocomplete script
install zkpolicy_autocomplete $RPM_BUILD_ROOT/etc/bash_completion.d

# Manpages
install manpages/* $RPM_BUILD_ROOT%{_mandir}/man1

# Default config files
cp -a conf/* $RPM_BUILD_ROOT%{install_path}/conf

%files
%config(noreplace) %dir %attr(0755, root, root) %{install_path}
%attr(0755, root, root) %{install_path}/%{name}.jar
%{_bindir}/zkpolicy
/etc/bash_completion.d/zkpolicy_autocomplete
%{_mandir}/man1/zkpolicy*
%{install_path}/conf/*

%pre -p /bin/sh

# Creating groups and users
getent group zookeeper 2>/dev/null >/dev/null || /usr/sbin/groupadd -r zookeeper
getent passwd zkpolicy 2>&1 > /dev/null || /usr/sbin/useradd -c "ZKPolicy" -s /bin/bash -g zookeeper -r zkpolicy 2> /dev/null || :

# Create log directory if it does not exist
if [[ ! -e "/var/log/zkpolicy" ]]; then
  /usr/bin/install -d -o zkpolicy -g zookeeper -m 0755 /var/log/zkpolicy
fi

if [[ ! -e "/var/log/zkpolicy/zkpolicy.log" ]]; then
  /usr/bin/touch /var/log/zkpolicy/zkpolicy.log
  /usr/bin/chown zkpolicy:zookeeper /var/log/zkpolicy/zkpolicy.log
  /usr/bin/chmod og+rw /var/log/zkpolicy/zkpolicy.log
fi

# Create rollback directory if it does not exist
if [[ ! -e "/opt/zkpolicy/rollback" ]]; then
  /usr/bin/install -d -o zkpolicy -g zookeeper -m 0755 /opt/zkpolicy/rollback
fi

%clean
rm -rf $RPM_BUILD_ROOT
rm -rf /var/log/zkpolicy

%post -p /bin/sh

%preun -p /bin/sh

%postun -p /bin/sh

%changelog
* Fri Jul 18 2020 Christos Arvanitis <christos.arvanitis@cern.ch> 1.0.1-8
- Add MIT License

* Wed Jun 17 2020 Christos Arvanitis <christos.arvanitis@cern.ch> 1.0.1-7
- Changed default YARN config for SASL user yarn

* Wed Jun 17 2020 Christos Arvanitis <christos.arvanitis@cern.ch> 1.0.1-3
- Add support for logical equation of IPv4 subnets and addresses

* Mon Jun 15 2020 Christos Arvanitis <christos.arvanitis@cern.ch> 1.0.1-2
- Add support for logical equation of ip subnets and addresses

* Fri Jun 12 2020 Christos Arvanitis <christos.arvanitis@cern.ch> 1.0.1-1
- Update to 1.0.1
- Add query and check descriptions in CLI results
- Add configuration options to exclude audit report sections
- Add negate functionality for checks
- Minor changes in CLI option descriptions

* Mon Apr 03 2020 Emil Kleszcz <emil.kleszcz@cern.ch> 1.0.0-1
- The first release of the ZK Policy Audit tool
