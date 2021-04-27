let successProductIconFile = null;
const ALLOWED_PROFILE_PICTURE_FILE_TYPES = ["image/png", "image/jpeg", "image/jpg"];
const MAX_PROFILE_PICTURE_FILE_SIZE = 5 * 1024 * 1024; // 5 Megabytes

$("#file").on("change", function () {
  showSelectedPicture(this);
});

function showSelectedPicture(fileInput) {
  const file = fileInput.files[0];
  const image = $(".profile-photo__upload-image");

  const fileSize = file.size;
  const fileType = file.type;

  if (ALLOWED_PROFILE_PICTURE_FILE_TYPES.indexOf(fileType) == -1) {
    showApplicationAlert("Upload error: Please make sure that you upload a jpg, jpeg or png file", ApplicationAlertType.ERROR);
    return;
  }

  if (fileSize > MAX_PROFILE_PICTURE_FILE_SIZE) {
    showApplicationAlert("This file can´t be uploaded because they exceed our 5 MB file size limit.", ApplicationAlertType.ERROR);
    return;
  }
  successProductIconFile = file;
  image.attr("src", URL.createObjectURL(file)).fadeIn();
}
