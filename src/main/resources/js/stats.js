/*
 * Copyright (C) 2015-2016 Łukasz Budnik <lukasz.budnik@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
var stats = stats || {}

stats.details = function() {
    var path = '/v1/stats/'

    var device = $('#device').val().trim()

    var url = path + device

    $.ajax({
      url: url,
      processData: false,
      contentType: false,
      success: function(data) {
        str = JSON.stringify(data, null, 4)
        $('#details').text(str)
        $('#charts').empty()
        if (data.charts !== undefined) {
            for (i = 0; i < data.charts.length; i++) {
                var img = $('<img>');
                img.attr('src', data.charts[i].render.svg);
                img.attr('title', data.charts[i].name)
                img.appendTo('#charts');
            }
        }
      },
      error: function(error) {
        alert('Got error ==> ' + error)
      }
    })
}

