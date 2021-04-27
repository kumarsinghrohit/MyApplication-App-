$(document).ready(()=>{
    validateMandatoryFields();
    $(".application-text-input__field").on("keyup", validateMandatoryFields);
    $("input#price").on('keyup',appendPricePrefix);
    $("#createAppLib").validate({
        ignore: ".ignore",
        onclick: false,
        onkeyup: false,
        onfocusout: false,
        focusInvalid: false,
        rules: {
            file: "required",
            name: {
                required: true,
                minlength: 4,
                maxlength: 35
            },
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
            price: {
                required: false,
                number: true,
                min: 0,
                max: 1000
            }
        },
        messages: {
            file: "Upload error\u003A Please make sure that you upload a jpg, jpeg or png file.",
            name: "The name should contain a minimum of 4 characters and a maximum of 35 characters.",
            version: "Please enter valid version number.",
            binary: "Please make sure that you upload a valid binary.",
            shortDescription: "Invalid short description. The short description should contain a minimum of 8 characters and a maximum of 80 characters.",
            longDescription: "Invalid long description. The long description should contain a minimum of 16 characters and a maximum of 4000 characters.",
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
            createApp();
        }
    });
    $("input#file").on('change', validateMandatoryFields);
    $("input#binary").on('change',(e) =>{
        const file = e.target.files[0];
        $("label.app-binary__file-name").text(file.name);
        $("label.app-binary__file-name").css('display','inline-block');
        $("div.app-binary").css('margin-top','1.0625rem');
        validateMandatoryFields();
    });
});

const createApp = () =>{
    const form = $("#createAppLib")[0];
    const formData = new FormData(form);
    const createAppButton = $("#create-app-lib-button");
    updateButtonState(createAppButton,true);
    
    for (let i in formDataObject) {
      formData.append("galleryImages", formDataObject[i]);
    }
    $.ajax({
        type: "POST",
        url: "/manager/apps/",
        data: formData,
        contentType: false,
        processData: false,
        cache: false,
        timeout: 60000,
        enctype: "multipart/form-data",
        success: data => {
            queueAlert(data, ApplicationAlertType.SUCCESS);
            setTimeout(() => {
            	const appType = $("#appType").val();
            	(appType == 'App') ? location.replace("/") : location.replace("/?type=Library");
            }, 1000);
            updateButtonState(createAppButton,false);
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
    const name = $("#name").val();
    const version = $("#version").val();
    const file = $("#file").val();
    const binary = $("#binary").val();

    if (!!name && !!version && !!file && !!binary) {
      $("#create-app-lib-button").attr("disabled", false);
    } else {
      $("#create-app-lib-button").attr("disabled", true);
    }
};

const updateButtonState = (button, isDisabled) => {
  $(button).prop('disabled',isDisabled);  
};