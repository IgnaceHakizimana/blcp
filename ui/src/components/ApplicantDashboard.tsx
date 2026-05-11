import { useState, useEffect } from 'react';
import { apiClient } from '../api/client';

interface Application {
  id: string;
  companyName: string;
  status: string;
  comments?: string;
  createdAt: string;
}

export default function ApplicantDashboard() {
  const [applications, setApplications] = useState<Application[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [newCompanyName, setNewCompanyName] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const fetchApplications = async () => {
    try {
      const response = await apiClient.get('/applications');
      setApplications(response.data);
    } catch (error) {
      console.error("Failed to fetch applications", error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleNewApplication = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newCompanyName.trim()) return;
    setIsSubmitting(true);

    try {
      await apiClient.post('/applications', { companyName: newCompanyName });
      setIsModalOpen(false);
      setNewCompanyName('');
      fetchApplications();
    } catch (error: any) {
      alert(error.response?.data?.message || 'Failed to create application');
    } finally {
      setIsSubmitting(false);
    }
  };

  useEffect(() => {
    fetchApplications();
  }, []);

  const handleSubmitApplication = async (id: string) => {
    try {
      await apiClient.post(`/applications/${id}/submit`);
      fetchApplications();
    } catch (error: any) {
      alert(error.response?.data?.message || 'Failed to submit application');
    }
  };

  const handleFileUpload = async (e: React.ChangeEvent<HTMLInputElement>, appId: string) => {
    const file = e.target.files?.[0];
    if (!file) return;

    if (file.size > 5 * 1024 * 1024) {
      alert("File exceeds the 5MB limit.");
      return;
    }

    const formData = new FormData();
    formData.append('file', file);

    try {
      await apiClient.post(`/applications/${appId}/documents`, formData);
      alert('Document uploaded successfully!');
      e.target.value = '';
    } catch (error: any) {
      alert(error.response?.data?.message || 'Failed to upload document');
    }
  };

  if (isLoading) return <div className="text-center py-10">Loading applications...</div>;

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-3xl font-bold text-brand-blue">My Applications</h1>
        <button
          onClick={() => setIsModalOpen(true)}
          className="bg-brand-blue text-white px-4 py-2 rounded shadow hover:opacity-90 transition"
        >
          New Application
        </button>
      </div>

      {applications.length === 0 ? (
        <div className="bg-white shadow rounded-lg p-8 text-center text-gray-500 border">
          You have no applications yet. Click "New Application" to start.
        </div>
      ) : (
        <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
          {applications.map((app) => (
            <div key={app.id} className="bg-white shadow-md rounded-lg p-6 border border-gray-200 flex flex-col">
              <h3 className="text-xl font-semibold text-brand-blue mb-2">{app.companyName}</h3>

              <div className="mb-4">
                <span className={`px-2.5 py-0.5 inline-flex text-xs leading-5 font-semibold rounded-full
                  ${app.status === 'SUBMITTED' || app.status === 'PENDING_APPROVAL' ? 'bg-yellow-100 text-yellow-800' :
                  app.status === 'APPROVED' ? 'bg-green-100 text-green-800' :
                    app.status === 'REJECTED' ? 'bg-red-100 text-red-800' :
                    app.status === 'INFO_REQUESTED' ? 'bg-orange-100 text-orange-800' :
                      'bg-gray-100 text-gray-800'}`}>
                  {app.status.replace('_', ' ')}
                </span>
              </div>

              <div className="text-sm text-gray-500 mb-2 flex-grow">
                Created: {new Date(app.createdAt).toLocaleDateString()}
              </div>

              {app.comments && (app.status === 'INFO_REQUESTED' || app.status === 'REJECTED') && (
                <div className="mt-2 mb-4 p-3 bg-red-50 rounded text-sm text-red-800 italic border-l-4 border-red-400">
                  <span className="font-bold">Regulator Comment:</span> "{app.comments}"
                </div>
              )}

              <div className="flex justify-end items-center space-x-3 border-t pt-4 mt-4">
                <button className="text-brand-blue hover:underline text-sm font-medium">
                  View Documents
                </button>
                {(app.status === 'DRAFT' || app.status === 'INFO_REQUESTED') && (
                  <>
                    <label
                      className="bg-gray-200 text-gray-800 px-3 py-1.5 rounded text-sm hover:bg-gray-300 cursor-pointer font-medium">
                      Upload Doc
                      <input
                        type="file"
                        className="hidden"
                        onChange={(e) => handleFileUpload(e, app.id)}
                      />
                    </label>
                    <button
                      onClick={() => handleSubmitApplication(app.id)}
                      className="bg-green-600 text-white px-3 py-1.5 rounded text-sm hover:bg-green-700"
                    >
                      Submit
                    </button>
                  </>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {isModalOpen && (
        <div className="fixed inset-0 bg-gray-500 bg-opacity-75 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-lg shadow-xl max-w-md w-full p-6">
            <h2 className="text-xl font-bold mb-4 text-brand-blue">Start New Application</h2>
            <form onSubmit={handleNewApplication}>
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Bank / Institution Name
                </label>
                <input
                  type="text"
                  required
                  className="w-full border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-brand-gold"
                  value={newCompanyName}
                  onChange={(e) => setNewCompanyName(e.target.value)}
                  placeholder="e.g. Kigali Finance Ltd"
                />
              </div>
              <div className="flex justify-end space-x-3 mt-6">
                <button
                  type="button"
                  onClick={() => setIsModalOpen(false)}
                  className="px-4 py-2 text-gray-600 hover:bg-gray-100 rounded"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={isSubmitting}
                  className="px-4 py-2 bg-brand-blue text-white rounded hover:opacity-90 disabled:opacity-50"
                >
                  {isSubmitting ? 'Creating...' : 'Create Draft'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

    </div>
  );
}
