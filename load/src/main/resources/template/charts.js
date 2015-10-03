$(function () {

    $('#executionTime').text(new Date(profile.executionTime). toLocaleString());
    $('#description').text(profile.description);


    $('#indicatorPanel').highcharts({
        chart: {
            type: 'column'
        },
        title: {
            text: 'Response Time Summary'
        },
        credits: {
            enabled: false
        },
        xAxis: {
            type: 'category',
            labels: {
                style: {
                    fontSize: '13px',
                    fontFamily: 'Verdana, sans-serif'
                }
            }
        },
        yAxis: {
            min: 0,
            title: {
                text: 'Number of responses'
            }
        },
        series: [{
            name: 'Response times',
            data: indicatorData
        }],
        legend: {
            enabled: false
        },
        dataLabels: {
            enabled: true,
            color: '#FFFFFF',
            align: 'right',
            style: {
                fontSize: '13px',
                fontFamily: 'Verdana, sans-serif'
            }
        }
    });

    $('#requestTypesPanel').highcharts({
        chart: {
            plotBackgroundColor: null,
            plotBorderWidth: null,
            plotShadow: false,
            type: 'pie'
        },
        title: {
            text: 'Request Type Percentages'
        },
        tooltip: {
            pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'
        },
        credits: {
            enabled: false
        },
        plotOptions: {
            pie: {
                allowPointSelect: true,
                cursor: 'pointer',
                dataLabels: {
                    enabled: false
                },
                showInLegend: true
            }
        },
        series: [{
            name: "Types",
            colorByPoint: true,
            data: requestTypesData
        }]
    });


    _.each(statistics, function(el){

        var row = $('<tr>');
        _.each(el, function(val){
            row.append($('<td>').text(val))
        });


        $('#statisticsPanel > table')
            .append(row);
    }) ;


    if (_.isEmpty(errors)){
        $('#errorsPanel > table')
                    .append("<tr><td>There are no errors</td><td></td><td></td></tr>");
    } else {
        _.each(errors, function(el){
            var row = $('<tr>');
            _.each(el, function(val){
                row.append($('<td>').text(val))
            });


            $('#errorsPanel > table')
                .append(row);
        });
    }



    $('#responseTimeDistributions').highcharts({
        chart: {
            type: 'area'
        },
        title: {
            text: 'Response Time Distributions'
        },
        subtitle: {
            text: "This chart does not display the 1 and 99th percentile outliers. Click on the legend below to enable or disable response types."
        },
        credits: {
            enabled: false
        },
        xAxis: {
            categories: responseTimeDistributions.categories,
            tickmarkPlacement: 'on',
            title: {
                text: "Response times"
            }
        },
        yAxis: {
            title: {
                text: 'Number of responses'
            },
        },
        plotOptions: {
            area: {
                marker: {
                    enabled: false,
                },
                stacking: 'normal',
                lineColor: '#666666',
                lineWidth: 1
            }
        },
        series: responseTimeDistributions.distributions
    });


    $('#userActivity').highcharts({
        chart: {
            zoomType: 'x'
        },
        title: {
            text: 'Active Users'
        },
        subtitle: {
            text: document.ontouchstart === undefined ?
                    'Click and drag in the plot area to zoom in' : 'Pinch the chart to zoom in'
        },
        credits: {
            enabled: false
        },
        xAxis: {
            type: 'datetime',
            title: {
                text: 'Time'
            }
        },
        yAxis: {
            title: {
                text: 'Number of users'
            }
        },
        legend: {
            enabled: false
        },
        plotOptions: {
            area: {

                marker: {
                    radius: 2
                },
                lineWidth: 1,
                states: {
                    hover: {
                        lineWidth: 1
                    }
                },
                threshold: null
            }
        },

        series: [{
            type: 'area',
            name: 'User activity',
            data: userActivity
        }]
    });


});