import org.grails.cli.interactive.completers.DomainClassCompleter

description( "Generates a controller that performs CRUD operations" ) {
  usage "grails generate-controller [DOMAIN CLASS]"
  completer DomainClassCompleter
}


if(args) {
    def sourceClass = source(args[0])

    if(sourceClass) {
      def model = model(sourceClass)

      render template: template('scaffolding/Controller.groovy'), 
             destination: file("grails-app/controllers/${model.packagePath}/${model.convention('Controller')}.groovy"),
             model: model

      render template: template('scaffolding/Spec.groovy'), 
             destination: file("src/test/groovy/${model.packagePath}/${model.convention('ControllerSpec')}.groovy"),
             model: model           

      addStatus "Scaffolding completed for $sourceClass"                                         

    }
    else {
      error "No domain class found"
    }

}
else {
    error "No domain class specified"
}
