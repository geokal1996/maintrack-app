import apiClient from "./client";

// Gia na steiloume arxeio prepei na xrisimopoiisoume FormData (oxi JSON).
// To axios vazei mono tou to sosto Content-Type me to boundary.
export function importFaultsFromExcel(file) {
  const formData = new FormData();
  formData.append("file", file);
  return apiClient
    .post("/api/faults/import", formData)
    .then((res) => res.data);
}

// To ypodeigma erxetai san binary (arxeio), gia auto zitame responseType "blob".
export function downloadImportTemplate() {
  return apiClient
    .get("/api/faults/import/template", { responseType: "blob" })
    .then((res) => {
      const url = window.URL.createObjectURL(new Blob([res.data]));
      const link = document.createElement("a");
      link.href = url;
      link.setAttribute("download", "maintrack-import-template.xlsx");
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    });
}
