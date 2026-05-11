export default function Footer() {
  return (
    <footer className="bg-white border-t border-gray-200 mt-auto">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        <div className="text-center text-sm text-gray-500">
          <p className="font-semibold text-bnr-blue">Bank Licensing & Compliance Portal</p>
          <p className="mt-1">&copy; {new Date().getFullYear()} National Bank of Rwanda. All rights reserved.</p>
        </div>
      </div>
    </footer>
  );
}
