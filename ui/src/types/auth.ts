export type Role = 'APPLICANT' | 'REVIEWER' | 'APPROVER';

export interface User {
  email: string;
  role: Role;
}
