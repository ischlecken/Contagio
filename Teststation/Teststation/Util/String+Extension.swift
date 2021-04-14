import Foundation

extension NSMutableData {
    func appendString(_ string: String) {
        if let d = string.data(using: .utf8) {
            self.append(d)
        }
    }
    
    func appendFileData(fieldName: String, fileName: String, mimeType: String, fileData: Data, using boundary: String) {
        self.appendString("--\(boundary)\r\n")
        self.appendString("Content-Disposition: form-data; name=\"\(fieldName)\"; filename=\"\(fileName)\"\r\n")
        self.appendString("Content-Type: \(mimeType)\r\n\r\n")
        self.append(fileData)
        self.appendString("\r\n")
    }
    
    func appendFormField(named name: String, value: String?, using boundary: String){
        if let v = value  {
            var fieldString = "--\(boundary)\r\n"
            
            fieldString += "Content-Disposition: form-data; name=\"\(name)\"\r\n"
            fieldString += "\r\n"
            fieldString += "\(v)\r\n"
            
            self.appendString(fieldString)
        }
    }
}
