JARFILE=$(shell find target/ -maxdepth 1 -name \*.jar -exec basename {} \;)
SPECFILE=$(shell find -maxdepth 1 -name \*.spec -exec basename {} \; )
REPOURL=git+ssh://git@gitlab.cern.ch:7999
# DB gitlab group
REPOPREFIX=/db
REPONAME=zookeeper-policy-audit-tool
# Username
USERNAME=$(shell klist|grep "principal:"|cut -d ' ' -f 3|cut -d '@' -f 1)

# Get all the package info from the corresponding spec file
PKGVERSION=$(shell awk '/^%define version/ { print $$3 }' ${SPECFILE})
PKGRELEASE=1
PKGNAME=$(shell awk '/^%define name/ { print $$3 }' ${SPECFILE})
PKGID=$(PKGNAME)-$(PKGVERSION)
TARFILE=$(PKGID).tar.gz

DIST_RPM=hdp7


sources:
	@echo $(PKGVERSION)
	@echo $(PKGID)
	@echo $(JARFILE)
	@echo $(TARFILE)

	rm -rf /tmp/$(PKGID)

	mkdir /tmp/$(PKGID)

	cp target/${JARFILE} /tmp/$(PKGID)
	cd /tmp ; pwd ; ls -la ; tar -cvzf $(TARFILE) $(PKGID)

	mv /tmp/$(TARFILE) .
	rm -rf /tmp/$(PKGID)

all: sources

clean:
	rm $(TARFILE)

spec:
	sed -e 's/%{_release}/${PKGRELEASE}/g' ${SPECFILE} > /tmp/${SPECFILE}

srpm: all spec
	rpmbuild -bs --define '_sourcedir $(PWD)' /tmp/${SPECFILE}

rpm: all spec
	rpmbuild -ba --define "_release ${PKGRELEASE}" --define '_sourcedir $(PWD)' /tmp/${SPECFILE}

scratch:
	koji build ${DIST_RPM} --nowait --scratch ${REPOURL}${REPOPREFIX}/${REPONAME}.git#$(shell git rev-parse HEAD)

build:
	koji build ${DIST_RPM} --nowait ${REPOURL}${REPOPREFIX}/${REPONAME}.git#$(shell git rev-parse HEAD)

kbuild: srpm
	koji --config=.koji build --wait ${DIST_RPM} /root/rpmbuild/SRPMS/${PKGID}*src.rpm

add-testing:
	koji --config=.koji add-pkg --owner=${USERNAME} ${DIST_RPM}-testing ${PKGNAME}||true

add-qa:
	koji --config=.koji add-pkg --owner=${USERNAME} ${DIST_RPM}-qa ${PKGNAME}||true

add-stable:
	koji --config=.koji add-pkg --owner=${USERNAME} ${DIST_RPM}-stable ${PKGNAME}||true

tag-qa:
	koji --config=.koji tag-build ${DIST_RPM}-qa ${PKGID}-${PKGRELEASE}.el7.cern||true

tag-stable:
	koji --config=.koji tag-build ${DIST_RPM}-stable ${PKGID}-${PKGRELEASE}.el7.cern||true
