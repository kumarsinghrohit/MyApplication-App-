$(document).ready(function() {
  $("#delete-button").on("click", function(e) {
    e.preventDefault();
    deleteAppLib(this);
  });

  const deleteAppLib = () => {
    const appId = $("#id").val();
    const type = $("#appType").val();
    $.ajax({
      type: "DELETE",
      url: "/manager/apps/" + appId,
      timeout: 35000,
      success: data => {
        queueAlert(data, ApplicationAlertType.SUCCESS);
        setTimeout(() => {
        	(type == 'App') ? location.replace("/") : location.replace("/?type=Library");
        }, 1000);
      },
      error: (xmlHttpRequest, textStatus, err) => {
          if(textStatus == 'timeout'){
              showApplicationAlert("Not able to process the request. Please try again.", ApplicationAlertType.ERROR);
              updateButtonState(updateAppButton, false);
          }
          if(xmlHttpRequest.status == 401){
              setTimeout(() => {
                  location.replace("/");
              }, 1000);
          }else{
              showApplicationAlert(xmlHttpRequest.responseText, ApplicationAlertType.ERROR);
              updateButtonState(updateAppButton,false);
          }
      }
    });
  };
});
