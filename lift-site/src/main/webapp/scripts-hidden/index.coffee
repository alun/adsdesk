angular.module "adsdesk", [], ["$locationProvider", (locationProvider) ->
  locationProvider.html5Mode true
]

Main = (scope, loc) ->
  scope.location = loc
  scope.$watch "location.path()", (path) ->
    path = path.replace("/", "").split("/")
    if (["index", "index.html"].indexOf(path[0]) != -1)
      loc.path("/")
    else
      path[0] ||= "main"
      mainSections = [
        "main"
        "bids"
        "service"
        "help"
        "blog"
      ]
      curSection =
        if (mainSections.indexOf(path[0]) == -1)
          scope.lastSection || "main"
        else
          path[0]

      scope.currentPath = path
      scope.lastSection = curSection
      scope.menu = {}
      scope.menu[curSection] = class: "active"
      scope.mainContent = curSection + ".tmpl"

Main.$inject = ["$scope", "$location"]

window.Controllers ||= []
window.Controllers.Main = Main