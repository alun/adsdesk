angular.module "adsdesk", [], ["$locationProvider", (locationProvider) ->
  locationProvider.html5Mode true
]

MainMenu = (scope, location, rootScope) ->
  sections = scope.sections = window.lift_menu.menu

  defaultSection = null
  for s in sections
    if s.uri.indexOf("/index") == 0
      defaultSection = s
      break

  rootScope.location = location
  rootScope.$watch "location.path()", (path) ->
    pathParts = path.replace("/", "").split("/")
    if ["", "index.html"].indexOf(pathParts[0]) != -1
      location.path("/index")
    else
      newSection = scope.currentSection || defaultSection

      for s in sections
        newSection = s if path.indexOf(s.uri) == 0

      if scope.currentSection != newSection
        s.cssClass = "" for s in sections
        newSection.cssClass = "active"
        scope.currentSection = newSection
        rootScope.mainContent = newSection.uri + ".tmpl"

        SPLITTER = " :: "
        titleParts = document.title.split(SPLITTER)
        document.title = [titleParts[0], newSection.text].join(SPLITTER)

MainMenu.$inject = ["$scope", "$location", "$rootScope"]

window.Controllers ||= []
window.Controllers.MainMenu = MainMenu