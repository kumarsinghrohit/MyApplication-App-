$(document).ready(() => {
  validateMandatoryFields();
  $(".application-text-input__field").on("keyup", validateMandatoryFields);
  $("input#price").on('keyup',appendPricePrefix);
  $("#addNewVersion").validate({
      ignore: ".ignore",
      onclick: false,
      onkeyup: false,
      onfocusout: false,
      focusInvalid: false,
      rules: {
          version: "required",
          binary: "required",
          shortDescription: {
              required: false,
              minlength: 8,
              maxlength: 80
          },
          longDescription: {
              required: false,
              minlength: 16,
              maxlength: 4000
          },
          whatsNewInVersion: {
              required: false,
              minlength: 16,
              maxlength: 4000
          },
          price: {
              required: false,
              number: true,
              min: 0,
              max: 1000
          }
      },
      messages: {
          version: "Please enter valid version number.",
          binary: "Please make sure that you upload a valid binary.",
          shortDescription: "Invalid short description. The short description should contain a minimum of 8 characters and a maximum of 80 characters.",
          longDescription: "Invalid long description. The long description should contain a minimum of 16 characters and a maximum of 4000 characters.",
          whatsNewInVersion: "Invalid What's new in version. The What's new in version description should contain a minimum of 16 characters and a maximum of 4000 characters.",
          price: "The price should be greater than or equal to 0 and less than or equal to 1000."
      },
      errorPlacement: (error, element) =>{
      },
      invalidHandler: (event, validator) => {
          let errorText = "";
          $.each(validator.errorMap, (k, v) =>{
              errorText = errorText + v + "<br/>";
          });
          showApplicationAlert(errorText, ApplicationAlertType.ERROR);
      },
      submitHandler: (form)=>{
          addNewVersion();
      }
  });
  
  
  $("input#binary").on('change',(e) =>{
      const file = e.target.files[0];
      $("label.app-binary__file-name").text(file.name);
      $("label.app-binary__file-name").css('display','inline-block');
      $("div.app-binary").css('margin-top','1.0625rem');
      validateMandatoryFields();
  });
});

const addNewVersion = () =>{
	const id = $("input#id").val();
    const form = $("#addNewVersion")[0];
    const formData = new FormData(form);
    const addVersionButton = $("#create-button");
    updateButtonState(addVersionButton,true);
    
    for (let i in formDataObject) {
      formData.append("galleryImages", formDataObject[i]);
    }
    $.ajax({
        type: "POST",
        url: "/manager/apps/"+ id +"/versions",
        data: formData,
        contentType: false,
        processData: false,
        cache: false,
        timeout: 60000,
        enctype: "multipart/form-data",
        success: data => {
            queueAlert(data, ApplicationAlertType.SUCCESS);
            setTimeout(() => {
            	location.replace("/apps/edit/"+id);
            }, 1000);
            updateButtonState(addVersionButton,false);
        },
        error: (xmlHttpRequest, textStatus, err) => {
            if(textStatus == 'timeout'){
                showApplicationAlert("Not able to process the request. Please try again.", ApplicationAlertType.ERROR);
                updateButtonState(updateAppButton, false);
            }
            if(xmlHttpRequest.status == 401){
                setTimeout(() => {
                    location.replace("/apps");
                }, 1000);
            }else{
                showApplicationAlert(xmlHttpRequest.responseText, ApplicationAlertType.ERROR);
                updateButtonState(updateAppButton,false);
            }
        }
    });  
};

const appendPricePrefix = ()=> {
  const price = $("#price").val();
  if(price){
	  $(".application-text-input__prefix--price").html('&euro;');
  }else {
	  $(".application-text-input__prefix--price").html('');
  }
};
	
const validateMandatoryFields = ()=> {
  const version = $("#version").val();
  const binary = $("#binary").val();
  if (!!version && !!binary) {
    $("#create-button").attr("disabled", false);
  } else {
    $("#create-button").attr("disabled", true);
  }
};

const updateButtonState = (button, isDisabled) => {
  $(button).prop('disabled',isDisabled);  
};