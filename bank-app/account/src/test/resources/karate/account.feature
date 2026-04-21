Feature: Account API

  Scenario: Get all accounts
    * url 'http://localhost:8081'
    Given path 'cuentas'
    When method GET
    Then status 200

  Scenario: Test account operations
    # Get any client
    * url 'http://localhost:8080'
    Given path 'clientes'
    When method GET
    Then status 200
    * def clientId = response[0].clienteId
    # Create
    * url 'http://localhost:8081'
    Given path 'cuentas'
    And request { tipo: 'CHECKING', saldoInicial: 1000, estado: true, clienteId: '#(clientId)' }
    When method POST
    Then status 201
    * def accountNumber = response.numeroCuenta
    And match response == { numeroCuenta: '#(accountNumber)', tipo: 'CHECKING', saldoInicial: 1000, estado: true, clienteId: '#(clientId)' }
    # Get
    Given path 'cuentas', accountNumber
    When method GET
    Then status 200
    And match response == { numeroCuenta: '#(accountNumber)', tipo: 'CHECKING', saldoInicial: 1000, estado: true, clienteId: '#(clientId)' }
    # Update
    Given path 'cuentas', accountNumber
    And request { tipo: 'SAVINGS', saldoInicial: 1000, estado: true, clienteId: '#(clientId)' }
    When method PUT
    Then status 200
    And match response == { numeroCuenta: '#(accountNumber)', tipo: 'SAVINGS', saldoInicial: 1000, estado: true, clienteId: '#(clientId)' }
    # Partial Update
    Given path 'cuentas', accountNumber
    And request { estado: false }
    When method PATCH
    Then status 200
    And match response.estado == false
    # Delete
    Given path 'cuentas', accountNumber
    When method DELETE
    Then status 200
