###
  Global script file. Included at every page.
  Adds site navigation control.
###

module = angular.module "adsdesk", [], ["$locationProvider", (locationProvider) ->
  locationProvider.html5Mode true
  locationProvider.hashPrefix '!'
]

module.run ["$rootScope", (rootScope) ->
  rootScope.__proto__.bindDataHandler = ->
    this.$watch "funcId", (v) =>
      this.sendData = window[v]
      rootScope.$on v, (e, response) =>
        this.responseHandler(response) if this.responseHandler?

  # global callback to be called by lift when response the ajax query
  window.ajaxCallback = (callbackName, response) ->
    rootScope.$emit(callbackName, response)

]

module.directive("loginDialog", ["$location", "$rootScope", (location, rootScope) ->
  (scope, element, attrs) ->
    scope.$watch "location.path()", (path) ->
      isLoginPath = path.indexOf("/login") != -1
      element.modal if isLoginPath then 'show' else 'hide'

    element.on 'hidden', () ->
      location.path(rootScope.currentSection.uri)

    scope.responseHandler = (v) ->
      scope.$apply -> scope.response = v
])

module.controller "MainMenu", ["$scope", "$location", "$rootScope",
  (scope, location, rootScope) ->
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

          sectionTitle = angular.element(newSection.text).text().trim()
          titleParts = document.title.split(TITLE_SEPARATOR)
          document.title = [titleParts[0], sectionTitle].join(TITLE_SEPARATOR)
]
