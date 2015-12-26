module.exports = function(grunt) {

    // Project configuration.
    grunt.initConfig({
        pkg: grunt.file.readJSON('package.json'),
        watch: {
            files: ['<%= jshint.files %>'],
            tasks: ['jshint']
        },
        jshint: {
            files: ['Gruntfile.js', 'src/main/webapp/scripts/**/*.js'],
            options: {
                globals: {
                    jQuery: true
                }
            }
        },
        concat: {
            build: {
                src: ['src/main/webapp/scripts/**/*.js'],
                dest: 'src/main/webapp/dest/scripts/build.js'
            }
        },
        uglify: {
            options: {
                banner: '/*! <%= pkg.name %> - v<%= pkg.version %> - ' + '<%= grunt.template.today("yyyy-mm-dd") %> */'
            },
            min: {
                src: 'src/main/webapp/dest/scripts/build.js',
                dest: 'src/main/webapp/dest/scripts/build.min.js'
            }
        },
        browserSync: {
            dev: {
                bsFiles: {
                    src : [
                        'target/secured-app-1.0-SNAPSHOT/index.html',
                        'target/secured-app-1.0-SNAPSHOT/view/*.html',
                        'target/secured-app-1.0-SNAPSHOT/assets/css/**/*.css',
                        'target/secured-app-1.0-SNAPSHOT/assets/images/**/*.{png,jpg,jpeg,gif,webp,svg}',
                        'target/secured-app-1.0-SNAPSHOT/scripts/**/*.js'
                    ]
                }
            },
            options: {
                proxy: "localhost:8080/ng-app"
            }
        }
    });

    // Load plugins
    grunt.loadNpmTasks('grunt-contrib-watch');
    grunt.loadNpmTasks('grunt-contrib-jshint');
    grunt.loadNpmTasks('grunt-contrib-concat');
    grunt.loadNpmTasks('grunt-contrib-uglify');
    grunt.loadNpmTasks('grunt-browser-sync');

    // Register complex task
    grunt.registerTask('default', ['concat', 'uglify']);
    grunt.registerTask('serve', ['browserSync', 'watch']);

};