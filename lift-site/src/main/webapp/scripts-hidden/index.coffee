angular.module "adsdesk", [], ["$locationProvider", "$routeProvider", (locationProvider, routeProvider) ->
  mainConf = templateUrl: "main.tmpl"
  routeProvider.when "/", mainConf
  routeProvider.when "/index", mainConf
  locationProvider.html5Mode true
]