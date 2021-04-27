let COUNT = 0;
let galleryImageUpdated = false;
const ALLOWED_GALLERY_IMAGE_FILE_TYPES = ["image/png", "image/jpeg", "image/jpg"];
const MAX_GALLERY_IMAGE_FILE_SIZE = 5 * 1024 * 1024; // 5 Megabytes
const formDataObject = {};

$(() => {
  $(".picture-placeholder").each(function () {
    initApplicationPlaceholder(this, COUNT++);
  });

  if (COUNT <= 4) {
    addNewGalleryPicture();
  }
});

const addNewGalleryPicture = () => {
  const newImage = $(createNewApplicationPicturePlaceholder());
  initApplicationPlaceholder(newImage, COUNT++);
  $(".application-well").append(newImage);
};

const initApplicationPlaceholder = (root, index) => {
  const fileInput = $(root).find("input[type=file]");
  const label = $(root).find("label");
  const image = $(root).find(".picture-placeholder__picture");
  fileInput.on("change", function () {
	  galleryImageUpdated = true;
	  validateMandatoryFields();
  });
  // Assign event handler to label to highlight the containing well when it is clicked
  $(root).on("click", function (e) {
    e.stopPropagation();

    $(this).parents(".application-well").addClass("application-well--highlighted");
  });

  const idToAssign = `gallery-image-${index}`;
  fileInput.attr("id", idToAssign);
  label.attr("for", idToAssign);

  fileInput.on("change", function () {
    const file = this.files[0];

    const fileSize = file.size;
    const fileType = file.type;

    if (ALLOWED_GALLERY_IMAGE_FILE_TYPES.indexOf(fileType) == -1) {
      showApplicationAlert("Upload error: Please make sure that you upload a jpg, jpeg or png file", ApplicationAlertType.ERROR);
      return;
    }

    if (fileSize > MAX_GALLERY_IMAGE_FILE_SIZE) {
      showApplicationAlert(
        "This file can´t be uploaded because they exceed our 5 MB file size limit.",
        ApplicationAlertType.ERROR
      );
      return;
    }

    image.attr("src", URL.createObjectURL(file)).css("display", "inline-block");
    formDataObject[index] = file;

    if (!$(root).hasClass("picture-placeholder--picture-selected") && COUNT < 5) {
      $(root).addClass("picture-placeholder--picture-selected");
      addNewGalleryPicture();
    }
  });
};

const createNewApplicationPicturePlaceholder = () => `
  <div class="picture-placeholder">
    <img class="picture-placeholder__picture" />
    <input type="file" style="display: none;" />
    <label class="picture-placeholder__bottom-strip-container d-flex justify-content-center align-items-center">
      <div class="picture-placeholder__bottom-strip"></div>
      <img src="/icons/picture.svg" alt="Change the picture" />
    </label>
  </div>
`;
