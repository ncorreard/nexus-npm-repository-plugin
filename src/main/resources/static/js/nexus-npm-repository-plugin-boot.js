define('nexus-npm-repository-plugin-boot', [
    'repository/npmRepositoryEditContribution'
], function () {
    NX.log.enabled = true;
    NX.log.levels['debug'] = true;

    NX.log.debug('Module loaded: nexus-npm-repository-plugin-boot');
});