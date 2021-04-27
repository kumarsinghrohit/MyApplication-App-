$(document).ready(function() {
    $(".app-application-icon__anchor")
    .on("mouseenter", function() {
        $(".app-application-icon").addClass("app-application-icon--highlighted");
        $(".app-application-icon__text").addClass("app-application-icon__text--highlighted");
    })
    .on("mouseleave", function() {
        $(".app-application-icon").removeClass("app-application-icon--highlighted");
        $(".app-application-icon__text").removeClass("app-application-icon__text--highlighted");
    });
});
