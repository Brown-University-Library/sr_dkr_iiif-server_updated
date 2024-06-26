/**
 * Wraps an application configuration object.
 *
 * @param data {Object} of key-value configuration key-value pairs
 * @constructor
 */
var Configuration = function(data) {

    /**
     * @returns {*}
     */
    this.data = function() {
        return data;
    };

    /**
     * @param key
     * @returns {*}
     */
    this.get = function(key) {
        return data[key];
    };

    /**
     * @returns {*}
     */
    this.keys = function() {
        return Object.keys(data);
    };

    /**
     * @param key {String}
     * @param value {*}
     */
    this.set = function(key, value) {
        data[key] = value;
    };

    /**
     * @returns {String}
     */
    this.toJsonString = function() {
        return JSON.stringify(data, null, 2);
    };

};

Configuration.ENDPOINT = $('input[name=cl-admin-uri]').val() + '/configuration';

/**
 * @param config {Configuration}
 * @constructor
 */
var Form = function(config) {

    var restart_required = false;
    var self = this;

    var attachEventListeners = function() {

        /**
         * Shows or hides all the other rows in the same table as a checkbox
         * element, depending on whether it is checked or not.
         *
         * @param checkbox_selector jQuery selector for one or multiple
         *                          checkboxes
         */
        var showOrHideAllOtherTableRows = function(checkbox_selector) {
            $(checkbox_selector).on('change', function() {
                var other_rows = $(this).parents('tr').nextAll('tr');
                if ($(this).prop('checked')) {
                    other_rows.show();
                } else {
                    other_rows.hide();
                }
            }).trigger('change');
        };

        ///////////////////// global form listeners /////////////////////////

        // When a form element that represents a property that requires
        // a restart is changed, save that fact.
        $('[data-requires-restart="true"], [data-requires-restart="1"]').
        on('click', function() {
            self.setRestartRequired(true);
        });

        // Override the form submit behavior to invoke custom validation and
        // save functions.
        $('input[type="submit"]').on('click', function() {
            if (self.validate()) {
                self.save($(this).parents('form'));
            }
            return false;
        });

        ////////////////////// individual sections //////////////////////////

        // Server
        showOrHideAllOtherTableRows(
            '[name="http.enabled"], [name="https.enabled"]');
        // Endpoints
        showOrHideAllOtherTableRows(
            '[name="endpoint.iiif.1.enabled"], [name="endpoint.iiif.2.enabled"]');
        // Source
        $('[name="source.delegate"]').on('change', function() {
            var other_rows = $(this).parents('tr').nextAll('tr');
            if ($(this).val() === 'false') {
                other_rows.show();
            } else {
                other_rows.hide();
            }
        }).trigger('change');
        // Processors
        $('[name="processor.selection_strategy"]').on('change', function() {
            var next_section = $(this).parents('section').next('section');
            if ($(this).val() === 'ManualSelectionStrategy') {
                next_section.show();
            } else {
                next_section.hide();
            }
        }).trigger('change');
        // Caches
        showOrHideAllOtherTableRows(
            '[name="cache.client.enabled"], [name="cache.server.worker.enabled"]');
        // Overlays
        showOrHideAllOtherTableRows(
            '[name="overlays.BasicStrategy.enabled"], [name="redaction.enabled"]');
        // Delegate Script
        showOrHideAllOtherTableRows('[name="delegate_script.enabled"]');
        // Logging
        showOrHideAllOtherTableRows(
            '[name="log.application.ConsoleAppender.enabled"], ' +
            '[name="log.application.FileAppender.enabled"], ' +
            '[name="log.application.RollingFileAppender.enabled"], ' +
            '[name="log.application.SyslogAppender.enabled"], ' +
            '[name="log.access.ConsoleAppender.enabled"], ' +
            '[name="log.access.FileAppender.enabled"], ' +
            '[name="log.access.RollingFileAppender.enabled"], ' +
            '[name="log.access.SyslogAppender.enabled"]');
    };

    /**
     * Updates the form state to correspond to that of the Configuration
     * instance.
     */
    this.load = function() {
        console.info('Loading configuration');
        console.debug(config.data());

        config.keys().forEach(function(key) {
            var configValue = config.get(key);
            var elements = $('[name="' + key + '"]');

            if (elements.is(":checkbox")) {
                elements.each(function() {
                    $(this).prop('checked', (configValue == 'true'));
                });
            } else {
                elements.val(configValue);
            }

            if (elements.type == 'text') {
                elements.val(configValue);
            } else if (elements.type == 'checkbox') {
                elements.forEach(function(e) {
                    $(this).prop('checked', ($(this).val() == configValue));
                });
            }
        });

        attachEventListeners();
    };

    this.setRestartRequired = function(bool) {
        restart_required = bool;
    };

    /**
     * Updates the Configuration instance to correspond with the form state.
     *
     * @param formElem Form element
     */
    this.save = function(formElem) {
        // Copy the values of non-checkbox form controls into the
        // configuration object.
        $(formElem).find('input, textarea, select').
        not('[type=submit], [type=checkbox]').each(function() {
            config.set($(this).attr('name'), $(this).val());
        });
        // Copy the checkbox values. This works only with one checkbox per
        // input name.
        $(formElem).find('input[type=checkbox]').each(function() {
            config.set($(this).attr('name'), $(this).is(':checked'));
        });

        console.info('Saving configuration');
        console.debug(config.data());

        $.ajax({
            type: 'PUT',
            contentType: 'application/json',
            url: Configuration.ENDPOINT,
            data: config.toJsonString(),
            success: function() {
                // Set the success message, make it appear, and fade it out on
                // a delay.
                var msg = '&check; Configuration saved.';
                if (restart_required) {
                    msg += ' A restart will be required for some changes to ' +
                        'take effect.';
                }
                var alert = $('<div class="alert alert-success">' + msg + '</div>');
                $(formElem).find('input[type="submit"]').before(alert);
                alert.delay(4000).fadeOut(800, function () {
                    restart_required = false;
                    alert.remove();
                });
            },
            error: function(xhr, status, error) {
                console.error(xhr);
                console.error(error);
                alert('Failed to save the configuration: ' + error);
            }
        });
    };

    /**
     * Checks the validity of the form and updates it with error messages if
     * invalid.
     *
     * @returns {Boolean}
     */
    this.validate = function() {
        // TODO: write this
        return true;
    };

};

var StatusUpdater = function() {

    var STATUS_ENDPOINT = $('input[name=cl-admin-uri]').val() + '/status';

    this.update = function() {
        var memoryStatusSection = $('#cl-status-memory');
        var cacheStatusSection = $('#cl-status-internal-caches');
        var vmStatusSection = $('#cl-status-vm');
        var tasksSection = $('#cl-tasks');

        $.ajax({
            dataType: 'json',
            url: STATUS_ENDPOINT,
            data: null,
            success: function(data) {
                console.debug(data);

                // Status section
                memoryStatusSection.find('tr:nth-child(1) > td:last-child')
                    .text(data.vm.usedHeapBytes + ' MB');
                memoryStatusSection.find('tr:nth-child(2) > td:last-child')
                    .text(data.vm.freeHeapBytes + ' MB');
                memoryStatusSection.find('tr:nth-child(3) > td:last-child')
                    .text(data.vm.totalHeapBytes + ' MB');
                memoryStatusSection.find('tr:nth-child(4) > td:last-child')
                    .text(data.vm.maxHeapBytes + ' MB');

                var usedPercent = data.vm.usedHeapPercent * 100;
                var memoryBarClass = 'progress-bar-success';
                if (usedPercent > 80) {
                    memoryBarClass = "progress-bar-danger";
                } else if (usedPercent > 70) {
                    memoryBarClass = "progress-bar-warning";
                }

                memoryStatusSection.find('div.progress-bar')
                    .attr('aria-valuenow', usedPercent)
                    .removeClass('progress-bar-success progress-bar-warning progress-bar-danger')
                    .addClass(memoryBarClass)
                    .css('width', usedPercent + '%')
                    .find('.sr-only')
                    .text(usedPercent + '% memory used');

                // Internal Caches section
                cacheStatusSection.find('tr:nth-child(1) > td:last-child')
                    .text(data.infoCache.size);
                cacheStatusSection.find('tr:nth-child(2) > td:last-child')
                    .text(data.infoCache.maxSize);

                // Tasks section
                if (data.tasks) {
                    var tasks = data.tasks // display the last 10
                        .slice(data.tasks.length - 10, data.tasks.length)
                        .reverse();
                    if (tasks.length > 0) {
                        var tbody = tasksSection.find('tbody');
                        tbody.empty();

                        tasks.forEach(function (t) {
                            var queued_at = t.queued_at ?
                                new Date(Date.parse(t.queued_at)) : null;
                            var started_at = t.started_at ?
                                new Date(Date.parse(t.started_at)) : null;
                            var stopped_at = t.stopped_at ?
                                new Date(Date.parse(t.stopped_at)) : null;
                            tbody.append('<tr>' +
                                '<td>' + t.verb + '</td>' +
                                '<td>' + (queued_at ? '<time datetime="' + t.queued_at + '">' +
                                    queued_at.toLocaleString() + '</time>' : '') + '</td>' +
                                '<td>' + (started_at ? '<time datetime="' + t.started_at + '">' +
                                    started_at.toLocaleString() + '</time>' : '') + '</td>' +
                                '<td>' + (stopped_at ? '<time datetime="' + t.stopped_at + '">' +
                                    stopped_at.toLocaleString() + '</time>' : '') + '</td>' +
                                '</tr>');
                        });
                        tasksSection.show();
                    } else {
                        tasksSection.hide();
                    }
                } else {
                    tasksSection.hide();
                }

                // VM info section
                vmStatusSection.find('tr:last-child > td:last-child')
                    .text(data.vm.uptime);

            },
            error: function(xhr, status, error) {
                console.error(xhr);
                console.error(status);
                console.error(error);
            }
        });
    };

};

$(document).ready(function() {
    $('.cl-help').popover({
        placement: 'auto',
        html: true
    });

    var updater = new StatusUpdater();
    updater.update();
    setInterval(function() {
        updater.update();
    }, 5000);

    // Download configuration data into a Configuration instance, and
    // initialize a Form instance on success.
    $.ajax({
        dataType: 'json',
        url: Configuration.ENDPOINT,
        data: null,
        success: function(data) {
            new Form(new Configuration(data)).load();
        },
        error: function(xhr, status, error) {
            console.error(xhr);
            console.error(status);
            console.error(error);
            alert('Failed to load the configuration: ' + error);
        }
    });
});
