###
  Global script file. Included at every page.
  Adds site navigation control.
###

module = angular.module "adsdesk", [], ["$locationProvider", (locationProvider) ->
  locationProvider.html5Mode true
]

module.directive("loginDialog", ["$location", "$rootScope", (location, rootScope) ->
  (scope, element, attrs) ->

    scope.$watch "location.path()", (path) ->
      isLoginPath = path.indexOf("/login") != -1
      element.modal if isLoginPath then 'show' else 'hide'

    element.on 'hide', ->
      scope.$apply -> location.path(rootScope.currentSection.uri)
])

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
      newSection = rootScope.currentSection || defaultSection

      for s in sections
        s.uri = if s.uri[s.uri.length - 1] == '/' then s.uri.substring(0, s.uri.length - 1) else s.uri
        newSection = s if path.indexOf(s.uri) == 0

      if rootScope.currentSection != newSection
        s.cssClass = "" for s in sections
        newSection.cssClass = "active"
        rootScope.currentSection = newSection
        rootScope.sectionTemplateUrl = newSection.uri + ".tmpl"

        SPLITTER = " :: "
        titleParts = document.title.split(SPLITTER)
        document.title = [titleParts[0], newSection.text].join(SPLITTER)

MainMenu.$inject = ["$scope", "$location", "$rootScope"]

window.Controllers ||= []
window.Controllers.MainMenu = MainMenu