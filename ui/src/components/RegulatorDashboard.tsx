import { useState, useEffect } from 'react';
import { apiClient } from '../api/client';
import { useAuth } from '../context/AuthContext';

interface Application {
  id: string;
  companyName: string;
  status: string;
  comments?: string;
  createdAt: string;
}

export default function RegulatorDashboard() {
  const [applications, setApplications] = useState<Application[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const { user } = useAuth();

  // Modal State
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [modalAction, setModalAction] = useState<{ id: string; type: 'request-info' | 'reject' } | null>(null);
  const [comment, setComment] = useState('');
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

  useEffect(() => {
    fetchApplications();
  }, []);

  const handleAction = async (id: string, action: string, payload?: any) => {
    try {
      await apiClient.post(`/applications/${id}/${action}`, payload);
      fetchApplications();
    } catch (error: any) {
      alert(error.response?.data?.message || 'Action failed');
    }
  };

  const openCommentModal = (id: string, type: 'request-info' | 'reject') => {
    setModalAction({ id, type });
    setComment('');
    setIsModalOpen(true);
  };

  const handleCommentSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!modalAction || !comment.trim()) return;

    setIsSubmitting(true);
    try {
      await apiClient.post(`/applications/${modalAction.id}/${modalAction.type}`, { comments: comment });
      setIsModalOpen(false);
      setModalAction(null);
      setComment('');
      fetchApplications();
    } catch (error: any) {
      alert(error.response?.data?.message || 'Action failed');
    } finally {
      setIsSubmitting(false);
    }
  };

  if (isLoading) return <div className="text-center py-10">Loading applications...</div>;

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-3xl font-bold text-brand-blue">Regulatory Dashboard</h1>
      </div>

      {applications.length === 0 ? (
        <div className="bg-white shadow rounded-lg p-8 text-center text-gray-500 border">
          No active applications in the system right now.
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
                Submitted: {new Date(app.createdAt).toLocaleDateString()}
              </div>

              {app.comments && (
                <div className="mt-2 mb-4 p-3 bg-gray-50 rounded text-sm text-gray-700 italic border-l-4 border-gray-300">
                  "{app.comments}"
                </div>
              )}

              <div className="flex justify-end items-center space-x-3 border-t pt-4 mt-4">

                {user?.role === 'REVIEWER' && app.status === 'SUBMITTED' && (
                  <button
                    onClick={() => handleAction(app.id, 'review')}
                    className="bg-brand-blue text-white px-3 py-1.5 rounded text-sm hover:opacity-90"
                  >
                    Start Review
                  </button>
                )}

                {user?.role === 'REVIEWER' && app.status === 'UNDER_REVIEW' && (
                  <>
                    <button
                      onClick={() => openCommentModal(app.id, 'request-info')}
                      className="bg-orange-500 text-white px-3 py-1.5 rounded text-sm hover:bg-orange-600"
                    >
                      Request Info
                    </button>
                    <button
                      onClick={() => handleAction(app.id, 'recommend-approval')}
                      className="bg-purple-600 text-white px-3 py-1.5 rounded text-sm hover:bg-purple-700"
                    >
                      Recommend Approval
                    </button>
                  </>
                )}

                {user?.role === 'APPROVER' && app.status === 'PENDING_APPROVAL' && (
                  <>
                    <button
                      onClick={() => openCommentModal(app.id, 'reject')}
                      className="bg-red-600 text-white px-3 py-1.5 rounded text-sm hover:bg-red-700"
                    >
                      Reject
                    </button>
                    <button
                      onClick={() => handleAction(app.id, 'approve')}
                      className="bg-green-600 text-white px-3 py-1.5 rounded text-sm hover:bg-green-700"
                    >
                      Approve
                    </button>
                  </>
                )}

              </div>
            </div>
          ))}
        </div>
      )}

      {/* COMMENT MODAL */}
      {isModalOpen && (
        <div className="fixed inset-0 bg-gray-500 bg-opacity-75 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-lg shadow-xl max-w-md w-full p-6">
            <h2 className="text-xl font-bold mb-4 text-brand-blue">
              {modalAction?.type === 'reject' ? 'Provide Rejection Reason' : 'Request More Information'}
            </h2>
            <form onSubmit={handleCommentSubmit}>
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Comments (Required)
                </label>
                <textarea
                  required
                  rows={4}
                  className="w-full border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-brand-gold"
                  value={comment}
                  onChange={(e) => setComment(e.target.value)}
                  placeholder="Please explain the decision or request..."
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
                  className={`px-4 py-2 text-white rounded hover:opacity-90 disabled:opacity-50 ${
                    modalAction?.type === 'reject' ? 'bg-red-600' : 'bg-orange-500'
                  }`}
                >
                  {isSubmitting ? 'Submitting...' : 'Submit'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

    </div>
  );
}
