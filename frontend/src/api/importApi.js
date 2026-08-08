import apiClient from "./client";

// 1o vima: "ti exei mesa auto to arxeio;" - stelnoume to arxeio kai mas epistrefei
// tis stiles tou, ena deigma grammon kai mia protasi antistoixisis. Den apothikevei tipota.
export function inspectExcel(file) {
  const formData = new FormData();
  formData.append("file", file);
  return apiClient
    .post("/api/faults/import/inspect", formData)
    .then((res) => res.data);
}

// Endiameso vima: poies mihanes anaferei to arxeio kai se poies dikes mas antistoixoun.
// Den grafei tipota sti vasi - mono protaseis gia epivevaiosi.
export function matchMachines(file, machineColumn) {
  const formData = new FormData();
  formData.append("file", file);
  formData.append("machineColumn", machineColumn);
  return apiClient
    .post("/api/faults/import/match-machines", formData)
    .then((res) => res.data);
}

// 2o vima: i eisagogi. To "mapping" einai proairetiko - to stelnoume mono otan
// o xristis exei orisei o idios poia stili einai ti.
export function importFaultsFromExcel(file, mapping) {
  const formData = new FormData();
  formData.append("file", file);
  if (mapping) {
    formData.append("mapping", JSON.stringify(mapping));
  }
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
