import { useState, useRef } from "react";
import { useQuery } from "@tanstack/react-query";
import { getAllBanks } from "../api/bankService";
import { singleUpload, previewBatch, batchUpload } from "../api/uploadService";
import StatusBadge from "../components/StatusBadge";

// Hardcoded for now — will come from auth in Phase 9
const CURRENT_USER_ID = "c92ab75b-b830-4515-becf-213ae889ca8c";

function UploadPage() {
  const [mode, setMode] = useState("single");

  return (
    <div className="max-w-xl mx-auto">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-2xl font-semibold text-white mb-1">
          Upload Disputes
        </h1>
        <p className="text-gray-400 text-sm">
          Upload dispute documents to bank portals — single or batch mode
        </p>
      </div>

      {/* Mode Toggle */}
      <div className="flex gap-2 mb-8">
        <button
          onClick={() => setMode("single")}
          className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
            mode === "single"
              ? "bg-blue-600 text-white"
              : "bg-gray-800 text-gray-400 hover:text-white"
          }`}
        >
          Single Upload
        </button>
        <button
          onClick={() => setMode("batch")}
          className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
            mode === "batch"
              ? "bg-blue-600 text-white"
              : "bg-gray-800 text-gray-400 hover:text-white"
          }`}
        >
          Batch Upload
        </button>
      </div>

      {mode === "single" ? <SingleUpload /> : <BatchUpload />}
    </div>
  );
}

// ─── Single Upload ────────────────────────────────────────
function SingleUpload() {
  const [file, setFile] = useState(null);
  const [bankId, setBankId] = useState("");
  const [caseId, setCaseId] = useState("");
  const [documentType, setDocumentType] = useState("REPRESENTATION");
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);
  const fileRef = useRef();

  const { data: banks = [] } = useQuery({
    queryKey: ["banks"],
    queryFn: getAllBanks,
    refetchInterval: false,
    staleTime: 1000 * 60 * 5, // cache for 5 minutes
  });

  const handleFileChange = (e) => {
    const selected = e.target.files[0];
    if (!selected) return;
    setFile(selected);
    setError(null);

    const name = selected.name.replace(".pdf", "").replace(".PDF", "");
    const parts = name.split("_");
    if (parts.length >= 2) {
      const prefix = parts[0].toUpperCase();
      // banks is already loaded from useQuery
      const detectedBank = banks.find((b) => b.filePrefix === prefix);
      if (detectedBank) {
        setBankId(detectedBank.id);
      }
      setCaseId(parts[1]);
    }
  };
  const handleSubmit = async () => {
    if (!file || !bankId || !caseId) {
      setError("Please provide a file, bank, and case ID");
      return;
    }

    setLoading(true);
    setError(null);
    setResult(null);

    try {
      const data = await singleUpload(
        file,
        CURRENT_USER_ID,
        bankId,
        caseId,
        documentType,
      );
      setResult(data);
      setFile(null);
      fileRef.current.value = "";
    } catch (err) {
      setError(err.response?.data?.message || "Upload failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-xl mx-auto">
      <div className="bg-gray-900 border border-gray-700 rounded-xl p-6 space-y-5">
        {/* File Upload */}
        <div>
          <label className="block text-sm font-medium text-gray-300 mb-2">
            PDF Document
          </label>
          <input
            ref={fileRef}
            type="file"
            accept=".pdf"
            onChange={handleFileChange}
            className="block w-full text-sm text-gray-400
              file:mr-4 file:py-2 file:px-4 file:rounded-lg
              file:border-0 file:text-sm file:font-medium
              file:bg-blue-600 file:text-white
              hover:file:bg-blue-700 cursor-pointer"
          />
          {file && (
            <p className="mt-2 text-xs text-gray-500">Selected: {file.name}</p>
          )}
        </div>

        {/* Bank Selector */}
        <div>
          <label className="block text-sm font-medium text-gray-300 mb-2">
            Bank
          </label>
          <select
            value={bankId}
            onChange={(e) => setBankId(e.target.value)}
            className="w-full bg-gray-800 border border-gray-600 rounded-lg
              px-3 py-2 text-white text-sm focus:outline-none
              focus:border-blue-500"
          >
            <option value="">Select bank...</option>
            {banks.map((bank) => (
              <option key={bank.id} value={bank.id}>
                {bank.name}
              </option>
            ))}
          </select>
        </div>

        {/* Case ID */}
        <div>
          <label className="block text-sm font-medium text-gray-300 mb-2">
            Case ID
          </label>
          <input
            type="text"
            value={caseId}
            onChange={(e) => setCaseId(e.target.value)}
            placeholder="e.g. 23gjh55"
            className="w-full bg-gray-800 border border-gray-600 rounded-lg
              px-3 py-2 text-white text-sm placeholder-gray-500
              focus:outline-none focus:border-blue-500"
          />
        </div>

        {/* Document Type */}
        <div>
          <label className="block text-sm font-medium text-gray-300 mb-2">
            Document Type
          </label>
          <select
            value={documentType}
            onChange={(e) => setDocumentType(e.target.value)}
            className="w-full bg-gray-800 border border-gray-600 rounded-lg
              px-3 py-2 text-white text-sm focus:outline-none
              focus:border-blue-500"
          >
            <option value="REPRESENTATION">Representation</option>
            <option value="CHARGEBACK">Chargeback</option>
            <option value="EVIDENCE">Evidence</option>
          </select>
        </div>

        {/* Error */}
        {error && (
          <div className="bg-red-500/10 border border-red-500/30 rounded-lg px-4 py-3">
            <p className="text-red-400 text-sm">{error}</p>
          </div>
        )}

        {/* Submit */}
        <button
          onClick={handleSubmit}
          disabled={loading}
          className="w-full bg-blue-600 hover:bg-blue-700 disabled:bg-blue-800
            disabled:cursor-not-allowed text-white font-medium py-2.5
            rounded-lg text-sm transition-colors"
        >
          {loading ? "Uploading..." : "Submit Upload"}
        </button>
      </div>

      {/* Result */}
      {result && (
        <div className="mt-6 bg-gray-900 border border-gray-700 rounded-xl p-6">
          <h3 className="text-white font-medium mb-4">Job Created</h3>
          <div className="space-y-3">
            <div className="flex justify-between items-center">
              <span className="text-gray-400 text-sm">Status</span>
              <StatusBadge status={result.status} />
            </div>
            <div className="flex justify-between items-center">
              <span className="text-gray-400 text-sm">Job ID</span>
              <span className="text-white text-xs font-mono">{result.id}</span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-gray-400 text-sm">Bank</span>
              <span className="text-white text-sm">{result.bankName}</span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-gray-400 text-sm">Case ID</span>
              <span className="text-white text-sm">{result.caseId}</span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-gray-400 text-sm">File</span>
              <span className="text-white text-sm">{result.fileName}</span>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

// ─── Batch Upload ─────────────────────────────────────────
function BatchUpload() {
  const [files, setFiles] = useState([]);
  const [previews, setPreviews] = useState([]);
  const [loading, setLoading] = useState(false);
  const [previewing, setPreviewing] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);
  const fileRef = useRef();

  const handleFilesChange = async (e) => {
    const selected = Array.from(e.target.files);
    if (!selected.length) return;

    setFiles(selected);
    setError(null);
    setPreviews([]);
    setResult(null);
    setPreviewing(true);

    try {
      const fileNames = selected.map((f) => f.name);
      const data = await previewBatch(fileNames);
      setPreviews(data.previews || []);
    } catch (err) {
      setError("Failed to preview files");
    } finally {
      setPreviewing(false);
    }
  };

  const handleSubmit = async () => {
    if (!files.length) return;
    setLoading(true);
    setError(null);

    try {
      const data = await batchUpload(CURRENT_USER_ID, files);
      setResult(data);
      setFiles([]);
      setPreviews([]);
      fileRef.current.value = "";
    } catch (err) {
      setError(err.response?.data?.message || "Batch upload failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <div className="bg-gray-900 border border-gray-700 rounded-xl p-6 mb-6">
        {/* File Upload */}
        <div className="mb-5">
          <label className="block text-sm font-medium text-gray-300 mb-2">
            Select PDF Files (multiple)
          </label>
          <input
            ref={fileRef}
            type="file"
            accept=".pdf"
            multiple
            onChange={handleFilesChange}
            className="block w-full text-sm text-gray-400
              file:mr-4 file:py-2 file:px-4 file:rounded-lg
              file:border-0 file:text-sm file:font-medium
              file:bg-blue-600 file:text-white
              hover:file:bg-blue-700 cursor-pointer"
          />
        </div>

        {/* Error */}
        {error && (
          <div className="bg-red-500/10 border border-red-500/30 rounded-lg px-4 py-3 mb-4">
            <p className="text-red-400 text-sm">{error}</p>
          </div>
        )}

        {/* Submit */}
        {previews.length > 0 && (
          <button
            onClick={handleSubmit}
            disabled={loading}
            className="w-full bg-blue-600 hover:bg-blue-700 disabled:bg-blue-800
              disabled:cursor-not-allowed text-white font-medium py-2.5
              rounded-lg text-sm transition-colors"
          >
            {loading ? "Submitting batch..." : `Submit ${files.length} files`}
          </button>
        )}
      </div>

      {/* Preview Table */}
      {previewing && (
        <div className="text-center py-8">
          <p className="text-gray-400 text-sm">Analysing files...</p>
        </div>
      )}

      {previews.length > 0 && (
        <div className="bg-gray-900 border border-gray-700 rounded-xl overflow-hidden">
          {/* Summary */}
          <div className="px-6 py-4 border-b border-gray-700 flex items-center justify-between">
            <h3 className="text-white font-medium">
              Preview — {previews.length} files
            </h3>
            <div className="flex gap-4 text-sm">
              <span className="text-green-400">
                ✓ {previews.filter((p) => p.is_ready).length} ready
              </span>
              <span className="text-yellow-400">
                ⚠ {previews.filter((p) => !p.is_ready).length} need review
              </span>
            </div>
          </div>

          {/* Table */}
          <table className="w-full">
            <thead>
              <tr className="border-b border-gray-700">
                <th className="text-left px-6 py-3 text-xs font-medium text-gray-400 uppercase">
                  Filename
                </th>
                <th className="text-left px-6 py-3 text-xs font-medium text-gray-400 uppercase">
                  Bank
                </th>
                <th className="text-left px-6 py-3 text-xs font-medium text-gray-400 uppercase">
                  Case ID
                </th>
                <th className="text-left px-6 py-3 text-xs font-medium text-gray-400 uppercase">
                  Reason Code
                </th>
                <th className="text-left px-6 py-3 text-xs font-medium text-gray-400 uppercase">
                  Status
                </th>
              </tr>
            </thead>
            <tbody>
              {previews.map((preview, index) => (
                <tr
                  key={index}
                  className="border-b border-gray-800 hover:bg-gray-800/50 transition-colors"
                >
                  <td className="px-6 py-4 text-sm text-white font-mono">
                    {preview.file_name}
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-300">
                    {preview.detected_bank || (
                      <span className="text-red-400">Not detected</span>
                    )}
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-300">
                    {preview.detected_case_id || (
                      <span className="text-red-400">Not detected</span>
                    )}
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-300">
                    {preview.detected_reason_code || (
                      <span className="text-gray-500">None</span>
                    )}
                  </td>
                  <td className="px-6 py-4">
                    {preview.is_ready ? (
                      <span className="text-xs bg-green-500/20 text-green-400 border border-green-500/30 px-2 py-1 rounded-md">
                        Ready
                      </span>
                    ) : (
                      <span
                        className="text-xs bg-yellow-500/20 text-yellow-400 border border-yellow-500/30 px-2 py-1 rounded-md"
                        title={preview.issue_reason}
                      >
                        Needs Review
                      </span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Batch Result */}
      {result && (
        <div className="mt-6 bg-green-500/10 border border-green-500/30 rounded-xl p-6">
          <h3 className="text-green-400 font-medium mb-3">
            Batch Submitted Successfully
          </h3>
          <div className="grid grid-cols-3 gap-4">
            <div className="text-center">
              <p className="text-2xl font-bold text-white">
                {result.totalFiles}
              </p>
              <p className="text-gray-400 text-xs mt-1">Total Files</p>
            </div>
            <div className="text-center">
              <p className="text-2xl font-bold text-blue-400">Processing</p>
              <p className="text-gray-400 text-xs mt-1">Status</p>
            </div>
            <div className="text-center">
              <p className="text-2xl font-bold text-white">
                {result.id?.slice(0, 8)}...
              </p>
              <p className="text-gray-400 text-xs mt-1">Batch ID</p>
            </div>
          </div>
          <p className="text-gray-400 text-sm mt-4 text-center">
            Check the Jobs dashboard to track progress
          </p>
        </div>
      )}
    </div>
  );
}

export default UploadPage;
