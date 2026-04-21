Feature: Account API

  Background:
    * url 'http://localhost:8080/api'

  Scenario: Get all clients
    Given path 'clientes'
    When method GET
    Then status 200

  Scenario: Test client operations
    # Create
    Given path 'clientes'
    And request { identityNumber: '11111', name: 'Ana Hernandez', gender: 'F', birthDate: '2000-01-01', address: 'Av 123', phone: '999222999', password: 'ashcbsdhcdsj', active: true }
    When method POST
    Then status 201
    * def clientId = response.clientId
    And match response == { clientId: '#(clientId)', identityNumber: '11111', name: 'Ana Hernandez', gender: 'F', birthDate: '2000-01-01', address: 'Av 123', phone: '999222999', password: 'ashcbsdhcdsj', active: true }
    # Get
    Given path 'clientes', clientId
    When method GET
    Then status 200
    And match response == { clientId: '#(clientId)', identityNumber: '11111', name: 'Ana Hernandez', gender: 'F', birthDate: '2000-01-01', address: 'Av 123', phone: '999222999', password: 'ashcbsdhcdsj', active: true }
    # Update
    Given path 'clientes', clientId
    And request { identityNumber: '22222', name: 'Laura Hernandez', gender: 'F', birthDate: '2001-01-01', address: 'Avenue 123', phone: '999222999', password: 'ashcbsdhcdsj', active: true }
    When method PUT
    Then status 200
    And match response == { clientId: '#(clientId)', identityNumber: '22222', name: 'Laura Hernandez', gender: 'F', birthDate: '2001-01-01', address: 'Avenue 123', phone: '999222999', password: 'ashcbsdhcdsj', active: true }
    # Partial Update
    Given path 'clientes', clientId
    And request { active: false }
    When method PATCH
    Then status 200
    And match response.active == false
    # Delete
    Given path 'clientes', clientId
    When method DELETE
    Then status 200
