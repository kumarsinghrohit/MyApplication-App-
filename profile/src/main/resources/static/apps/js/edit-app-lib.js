var fileInput = "";
var selectedFile = "";
var initialNamevalue = "";
$(document).ready(() => {
  initialNamevalue = $("#name").val();
  validateMandatoryFields();
  $(".application-text-input__field").on("keyup", validateMandatoryFields);
  $("#editDevelopedAppLibForm").validate({
    ignore: ".ignore",
    onclick: false,
    onkeyup: false,
    onfocusout: false,
    focusInvalid: false,
    rules: {
      name: {
        required: true,
        minlength: 4,
        maxlength: 35,
      },
    },
    messages: {
      name: "The name should contain a minimum of 4 characters and a maximum of 35 characters.",
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
      updateApp();
    },
  });
  $("input#file").on("change", validateMandatoryFields);
});

const updateApp = () => {
  const form = $("#editDevelopedAppLibForm")[0];
  const formData = new FormData();
  const updateAppButton = $("#update-button");
  updateButtonState(updateAppButton, true);

  const id = $("#id").val();
  const type = $("#appType").val();
  const developerId = $("#developerId").val();
  const name = $("#name").val();
  formData.append("name", name);
  formData.append("appType", type);
  formData.append("developerId", developerId);
  if (successProductIconFile) {
    formData.append("image", successProductIconFile);
    $("#update-button").attr("disabled", false);
  }

  $.ajax({
    type: "PUT",
    url: "/manager/apps/" + id,
    data: formData,
    contentType: false,
    processData: false,
    cache: false,
    timeout: 35000,
    enctype: "multipart/form-data",
    success: (data) => {
      var image = document.getElementById("displayAppLibIcon");
      if (!!selectedFile) {
        image.src = URL.createObjectURL(selectedFile);
      }
      queueAlert(data, ApplicationAlertType.SUCCESS);
      setTimeout(() => {
        type == "App" ? location.replace("/") : location.replace("/?type=Library");
      }, 1000);
      updateButtonState(updateAppButton, false);
    },
    error: (xmlHttpRequest, textStatus, err) => {
      $(fileInput).val(null);
      if (textStatus == "timeout") {
        showApplicationAlert("Not able to process the request. Please try again.", ApplicationAlertType.ERROR);
        updateButtonState(updateAppButton, false);
      }
      if (xmlHttpRequest.status == 401) {
        setTimeout(() => {
          location.replace("/");
        }, 1000);
      } else {
        showApplicationAlert(xmlHttpRequest.responseText, ApplicationAlertType.ERROR);
        updateButtonState(updateAppButton, false);
      }
    },
  });
};

const validateMandatoryFields = () => {
  const name = $("#name").val();
  if (successProductIconFile) {
    $("#update-button").attr("disabled", false);
  } else if (initialNamevalue.toLowerCase() !== name.toLowerCase()) {
    $("#update-button").attr("disabled", false);
  } else {
    $("#update-button").attr("disabled", true);
  }
};

const updateButtonState = (button, isDisabled) => {
  $(button).prop("disabled", isDisabled);
};
