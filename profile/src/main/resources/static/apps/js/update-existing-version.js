var initialPriceValue = "";
var initialShortDescriptionValue = "";
var initialLongDescriptionValue = "";
var initialUpdateInfoValue = "";
var initialVersionNumber = "";
$(document).ready(() => {
  initialPriceValue = $("#price").val();
  initialShortDescriptionValue = $("#shortDescription").val();
  initialLongDescriptionValue = $("#longDescription").val();
  initialUpdateInfoValue = $("#whatsNewInVersion").val();
  initialVersionNumber = $("#version").val();
  validateMandatoryFields();
  $(".application-text-input__field").on("keyup", validateMandatoryFields);
  $(".application-text-area__field").on("keyup", validateMandatoryFields);

  $("#updateExistingVersion").validate({
    ignore: ".ignore",
    onclick: false,
    onkeyup: false,
    onfocusout: false,
    focusInvalid: false,
    rules: {
      version: "required",
      shortDescription: {
        required: false,
        minlength: 8,
        maxlength: 80,
      },
      longDescription: {
        required: false,
        minlength: 16,
        maxlength: 4000,
      },
      whatsNewInVersion: {
        required: false,
        minlength: 16,
        maxlength: 4000,
      },
      price: {
        required: false,
        number: true,
        min: 0,
        max: 1000,
      },
    },
    messages: {
      version: "Please enter valid version number.",
      binary: "Please make sure that you upload a valid binary.",
      shortDescription:
        "Invalid short description. The short description should contain a minimum of 8 characters and a maximum of 80 characters.",
      longDescription:
        "Invalid long description. The long description should contain a minimum of 16 characters and a maximum of 4000 characters.",
      whatsNewInVersion:
        "Invalid What's new in version. The What's new in version description should contain a minimum of 16 characters and a maximum of 4000 characters.",
      price: "The price should be greater than or equal to 0 and less than or equal to 1000.",
    },
    errorPlacement: (error, element) => {},
    invalidHandler: (event, validator) => {
      let errorText = "";
      $.each(validator.errorMap, (k, v) => {
        errorText = errorText + v + "<br/>";
      });
      showApplicationAlert(errorText, ApplicationAlertType.ERROR);
    },
    submitHandler: (form) => {
      updateExistingVersion();
    },
  });

  $("input#binary").on("change", (e) => {
    const file = e.target.files[0];
    $("label.app-binary__file-name").text(file.name);
    $("label.app-binary__file-name").css("display", "inline-block");
    $("div.app-binary").css("margin-top", "1.0625rem");
    validateMandatoryFields();
  });
});

const updateExistingVersion = () => {
  const form = $("#updateExistingVersion")[0];
  const formData = new FormData(form);
  const addVersionButton = $("#update-version-button");
  updateButtonState(addVersionButton, true);

  for (let i in formDataObject) {
    formData.append("galleryImageIndexesToUpdate", i);
    formData.append("galleryImages", formDataObject[i]);
  }
  $.ajax({
    type: "PUT",
    url: "/manager/apps/" + appId + "/versions/" + versionId,
    data: formData,
    contentType: false,
    processData: false,
    cache: false,
    timeout: 60000,
    enctype: "multipart/form-data",
    success: (data) => {
      queueAlert(data, ApplicationAlertType.SUCCESS);
      setTimeout(() => {
        location.replace("/apps/edit/" + appId);
      }, 1000);
      updateButtonState(addVersionButton, false);
    },
    error: (xmlHttpRequest, textStatus, err) => {
      if (textStatus == "timeout") {
        showApplicationAlert("Not able to process the request. Please try again.", ApplicationAlertType.ERROR);
        updateButtonState(updateAppButton, false);
      }
      if (xmlHttpRequest.status == 401) {
        setTimeout(() => {
          location.replace("/apps");
        }, 1000);
      } else {
        showApplicationAlert(xmlHttpRequest.responseText, ApplicationAlertType.ERROR);
        updateButtonState(updateAppButton, false);
      }
    },
  });
};

const validateMandatoryFields = () => {
  const priceValue = $("#price").val();
  const shortDescriptionValue = $("#shortDescription").val();
  const longDescriptionValue = $("#longDescription").val();
  const updateInfoValue = $("#whatsNewInVersion").val();
  const versionNumber = $("#version").val();
  const binary = $("#binary").val();
  if (
    galleryImageUpdated ||
    !!binary ||
    initialVersionNumber.toLowerCase() !== versionNumber.toLowerCase() ||
    parseFloat(initialPriceValue) !== parseFloat(priceValue) ||
    initialShortDescriptionValue.toLowerCase() !== shortDescriptionValue.toLowerCase() ||
    initialLongDescriptionValue.toLowerCase() !== longDescriptionValue.toLowerCase() ||
    initialUpdateInfoValue.toLowerCase() !== updateInfoValue.toLowerCase()
  ) {
    $("#update-version-button").attr("disabled", false);
  } else {
    $("#update-version-button").attr("disabled", true);
  }
};

const updateButtonState = (button, isDisabled) => {
  $(button).prop("disabled", isDisabled);
};
