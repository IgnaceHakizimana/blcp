import { useState, useEffect } from 'react';
import { apiClient } from '../api/client';
import { useAuth } from '../context/AuthContext';

interface Application {
  id: string;
  companyName: string;
  status: string;
  createdAt: string;
}

export default function RegulatorDashboard() {
  const [applications, setApplications] = useState<Application[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const { user } = useAuth();

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

  useEffect(() => {
    fetchApplications();
  }, []);

  const handleAction = async (id: string, action: string) => {
    try {
      await apiClient.post(`/applications/${id}/${action}`);
      fetchApplications();
    } catch (error: any) {
      alert(error.response?.data?.message || 'Action failed');
    }
  };

  if (isLoading) return <div className="text-center py-10">Loading applications...</div>;

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-3xl font-bold text-gray-900">Regulatory Dashboard</h1>
      </div>

      {applications.length === 0 ? (
        <div className="bg-white shadow rounded-lg p-6 text-center text-gray-500">
          No active applications in the system right now.
        </div>
      ) : (
        <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
          {applications.map((app) => (
            <div key={app.id} className="bg-white shadow rounded-lg p-6 border border-gray-200">
              <h3 className="text-xl font-semibold text-gray-900 mb-2">{app.companyName}</h3>

              <div className="mb-4">
                <span className="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-gray-100 text-gray-800">
                  {app.status}
                </span>
              </div>

              <div className="text-sm text-gray-500 mb-6">
                Submitted: {new Date(app.createdAt).toLocaleDateString()}
              </div>

              {/* ROLE-BASED ACTION BUTTONS */}
              <div className="flex justify-end space-x-3 border-t pt-4">

                {user?.role === 'REVIEWER' && app.status === 'SUBMITTED' && (
                  <button
                    onClick={() => handleAction(app.id, 'review')}
                    className="bg-blue-600 text-white px-3 py-1 rounded text-sm hover:bg-blue-700"
                  >
                    Start Review
                  </button>
                )}

                {user?.role === 'REVIEWER' && app.status === 'UNDER_REVIEW' && (
                  <>
                    <button
                      onClick={() => handleAction(app.id, 'request-info')}
                      className="bg-orange-600 text-white px-3 py-1 rounded text-sm hover:bg-orange-700"
                    >
                      Request Info
                    </button>
                    <button
                      onClick={() => handleAction(app.id, 'recommend-approval')}
                      className="bg-purple-600 text-white px-3 py-1 rounded text-sm hover:bg-purple-700"
                    >
                      Recommend Approval
                    </button>
                  </>
                )}

                {user?.role === 'APPROVER' && app.status === 'PENDING_APPROVAL' && (
                  <button
                    onClick={() => handleAction(app.id, 'approve')}
                    className="bg-green-600 text-white px-3 py-1 rounded text-sm hover:bg-green-700"
                  >
                    Approve
                  </button>
                )}

              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
